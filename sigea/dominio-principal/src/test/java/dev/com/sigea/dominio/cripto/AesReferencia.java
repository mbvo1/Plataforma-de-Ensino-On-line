package dev.com.sigea.dominio.cripto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * Baseline AES-256 dos ataques da Fase 2, em duas formas.
 *
 * 1. AES-256-GCM pelo JCE, com os mesmos parametros do CpfCryptoConverter
 *    (chave de 256 bits, nonce de 12 bytes, tag de 128 bits). O conversor
 *    mora em infraestrutura e depende de Spring, e dominio-principal nao pode
 *    depender dela; por isso a configuracao e reproduzida aqui em vez de
 *    importada. Diferenca proposital: o conversor deriva o nonce do CPF
 *    (para manter UNIQUE e busca exata); aqui o nonce e aleatorio, como na
 *    CifraSigea, para comparar cifra com cifra e nao com uma escolha de
 *    projeto especifica do CPF.
 *
 * 2. AES-256 de bloco com numero de rodadas reduzivel, implementado aqui.
 *    Existe porque o painel "avalanche por rodada, cifra propria contra AES"
 *    precisa do AES com 1, 2, 3... rodadas, e o JCE so oferece as 14. A
 *    implementacao e validada contra o vetor C.3 do FIPS-197 e contra o JCE
 *    com 14 rodadas (AesReferenciaTest); sem essa validacao, a curva do AES
 *    no relatorio seria de uma cifra qualquer com nome de AES.
 *
 *    Convencao das rodadas reduzidas, a mesma do FIPS-197 e da literatura de
 *    criptoanalise: AddRoundKey inicial, r-1 rodadas completas e uma rodada
 *    final sem MixColumns. Com r = 14 isso e exatamente o AES-256.
 *
 * Codigo de teste, sem preocupacao com tempo constante ou desempenho: nunca
 * usar para cifrar dado real.
 */
final class AesReferencia {

    static final int RODADAS_AES_256 = 14;
    private static final int[] SBOX = gerarSbox();

    private AesReferencia() {
    }

    // ------------------------------------------------------------------
    // JCE
    // ------------------------------------------------------------------

    /** AES-256-GCM: devolve ciphertext || tag de 16 bytes, como o JCE entrega. */
    static byte[] gcmCifrar(byte[] chave, byte[] nonce, byte[] textoClaro) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chave, "AES"), new GCMParameterSpec(128, nonce));
            return cipher.doFinal(textoClaro);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** AES-256 em ECB, sem padding: so para demonstrar o vazamento de padrao do ECB. */
    static byte[] ecbCifrar(byte[] chave, byte[] textoClaroMultiplo16) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chave, "AES"));
            return cipher.doFinal(textoClaroMultiplo16);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------
    // AES-256 de bloco com rodadas reduzidas
    // ------------------------------------------------------------------

    static byte[] cifrarBloco(byte[] chave, byte[] bloco, int rodadas) {
        if (chave.length != 32 || bloco.length != 16) {
            throw new IllegalArgumentException("AES-256: chave de 32 bytes e bloco de 16");
        }
        if (rodadas < 1 || rodadas > RODADAS_AES_256) {
            throw new IllegalArgumentException("Rodadas entre 1 e 14");
        }
        int[] w = expandirChave(chave);
        // Estado em ordem de coluna: s[linha + 4*coluna], que coincide com a
        // ordem dos bytes de entrada no FIPS-197.
        byte[] s = bloco.clone();
        adicionarChaveRodada(s, w, 0);
        for (int r = 1; r <= rodadas; r++) {
            for (int i = 0; i < 16; i++) {
                s[i] = (byte) SBOX[s[i] & 0xFF];
            }
            deslocarLinhas(s);
            if (r < rodadas) {
                // Mesma MixColumns usada pela CifraSigea - validada contra o
                // FIPS-197 em CamadaDifusaoTest e, aqui, pelo AES inteiro.
                CamadaDifusao.MIXCOLUMNS_MDS.aplicar(s);
            }
            adicionarChaveRodada(s, w, r);
        }
        return s;
    }

    private static void adicionarChaveRodada(byte[] s, int[] w, int rodada) {
        for (int c = 0; c < 4; c++) {
            int palavra = w[4 * rodada + c];
            for (int linha = 0; linha < 4; linha++) {
                s[linha + 4 * c] ^= (byte) (palavra >>> (24 - 8 * linha));
            }
        }
    }

    private static void deslocarLinhas(byte[] s) {
        byte[] t = s.clone();
        for (int linha = 1; linha < 4; linha++) {
            for (int c = 0; c < 4; c++) {
                s[linha + 4 * c] = t[linha + 4 * ((c + linha) % 4)];
            }
        }
    }

    /** Expansao de chave do AES-256: Nk = 8, 60 palavras para 15 chaves de rodada. */
    private static int[] expandirChave(byte[] chave) {
        int[] w = new int[60];
        for (int i = 0; i < 8; i++) {
            w[i] = ((chave[4 * i] & 0xFF) << 24) | ((chave[4 * i + 1] & 0xFF) << 16)
                    | ((chave[4 * i + 2] & 0xFF) << 8) | (chave[4 * i + 3] & 0xFF);
        }
        int rcon = 1;
        for (int i = 8; i < 60; i++) {
            int t = w[i - 1];
            if (i % 8 == 0) {
                t = subPalavra(Integer.rotateLeft(t, 8)) ^ (rcon << 24);
                rcon = CamadaDifusao.mul(rcon, 2);
            } else if (i % 8 == 4) {
                t = subPalavra(t);
            }
            w[i] = w[i - 8] ^ t;
        }
        return w;
    }

    private static int subPalavra(int palavra) {
        return (SBOX[(palavra >>> 24) & 0xFF] << 24) | (SBOX[(palavra >>> 16) & 0xFF] << 16)
                | (SBOX[(palavra >>> 8) & 0xFF] << 8) | SBOX[palavra & 0xFF];
    }

    /**
     * S-box do AES calculada pela definicao (inverso em GF(2^8) seguido da
     * transformacao afim com 0x63), em vez de colada como tabela: uma tabela
     * de 256 numeros copiada a mao esconde erro de digitacao; a definicao nao.
     */
    private static int[] gerarSbox() {
        int[] s = new int[256];
        for (int x = 0; x < 256; x++) {
            int inv = x == 0 ? 0 : inverso(x);
            int b = inv ^ rotl8(inv, 1) ^ rotl8(inv, 2) ^ rotl8(inv, 3) ^ rotl8(inv, 4) ^ 0x63;
            s[x] = b & 0xFF;
        }
        return s;
    }

    /** x^254 = x^-1 em GF(2^8), pelo pequeno teorema de Fermat no grupo multiplicativo de ordem 255. */
    private static int inverso(int x) {
        int resultado = 1;
        int base = x;
        int e = 254;
        while (e > 0) {
            if ((e & 1) != 0) {
                resultado = CamadaDifusao.mul(resultado, base);
            }
            base = CamadaDifusao.mul(base, base);
            e >>>= 1;
        }
        return resultado;
    }

    private static int rotl8(int v, int n) {
        return ((v << n) | (v >>> (8 - n))) & 0xFF;
    }

    static int sbox(int x) {
        return SBOX[x & 0xFF];
    }
}
