package dev.com.sigea.dominio.cripto;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Escalonamento de chaves da CifraSigea - o requisito de "chave dinamica".
 *
 *     K_r = SHA-256(chaveMestra || nonce || r)[0..15],  r = 1..rodadas
 *     IV  = SHA-256(chaveMestra || nonce || 0)[0..15]
 *
 * Por que o nonce entra no escalonamento: a chave mestra fica parada no
 * ambiente, mas as subchaves que de fato tocam o bloco mudam a cada
 * mensagem. Duas cifragens do mesmo texto com a mesma chave mestra rodam,
 * na pratica, com chaves de rodada diferentes - e isso que atende ao "a
 * chave nao pode ser fixa" do enunciado, e o teste de ciphertexts
 * distintos e a evidencia.
 *
 * Por que o IV usa r = 0: as rodadas comecam em 1, entao o indice 0 fica
 * livre e o IV sai da mesma formula, sem um rotulo extra inventado fora da
 * especificacao. Derivar o IV com a chave (e nao usar o nonce cru) importa:
 * no CBC, um IV que o atacante consegue prever antes de escolher o texto
 * claro abre ataque de texto claro escolhido sobre o primeiro bloco (o
 * mesmo problema do TLS 1.0 explorado pelo BEAST). Sem a chave, o IV e
 * imprevisivel mesmo com o nonce publico no ciphertext.
 *
 * Por que SHA-256 truncado e nao uma KDF propria (HKDF): HKDF seria o
 * correto num sistema real; aqui o enunciado pede a derivacao explicita,
 * e SHA-256 sobre entradas de tamanho fixo (32 + 12 + 4 bytes) nao sofre
 * ambiguidade de concatenacao nem extensao de comprimento que importe,
 * porque nenhum valor derivado e usado como MAC.
 *
 * {@code r} e codificado em 4 bytes big-endian.
 */
public final class EscalonadorChaves {

    public static final int TAMANHO_SUBCHAVE = 16;

    private final byte[][] subchaves;
    private final byte[] iv;

    public EscalonadorChaves(byte[] chaveMestra, byte[] nonce, int rodadas) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
        }
        this.iv = derivar(sha256, chaveMestra, nonce, 0);
        this.subchaves = new byte[rodadas][];
        for (int r = 1; r <= rodadas; r++) {
            subchaves[r - 1] = derivar(sha256, chaveMestra, nonce, r);
        }
    }

    /** Subchave da rodada {@code r}, com r de 1 a rodadas. Sem copia: uso interno no laco quente da cifra. */
    byte[] subchave(int r) {
        return subchaves[r - 1];
    }

    byte[] iv() {
        return iv;
    }

    /** Copia publica, para os experimentos inspecionarem que as subchaves mudam com o nonce. */
    public byte[] copiaSubchave(int r) {
        return subchaves[r - 1].clone();
    }

    public int rodadas() {
        return subchaves.length;
    }

    private static byte[] derivar(MessageDigest sha256, byte[] chaveMestra, byte[] nonce, int r) {
        sha256.reset();
        sha256.update(chaveMestra);
        sha256.update(nonce);
        sha256.update(ByteBuffer.allocate(Integer.BYTES).putInt(r).array());
        return Arrays.copyOf(sha256.digest(), TAMANHO_SUBCHAVE);
    }
}
