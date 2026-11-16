package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fase 2b - ataque diferencial: efeito avalanche.
 *
 * Virar um bit (do texto claro ou da chave) e medir quantos bits do
 * ciphertext mudam. Uma cifra ideal se comporta como binomial(128; 0,5):
 * media de 50%, desvio de cerca de 4,4% (5,66 bits), e quase nunca abaixo
 * de 30%. Media perto de 50% e necessaria mas nao suficiente - o Adendo 01
 * mostrou uma cifra com media 49,6% e minimo de 1 bit - por isso o CSV traz
 * minimo, maximo e o histograma inteiro, nao so a media.
 *
 * Decisoes de metodo:
 *  - Funcao de bloco, nunca mensagem CBC multibloco. O CBC so propaga para
 *    frente; a media sobre mensagens longas descreveria o modo de operacao
 *    e o tamanho da mensagem. O efeito do modo e medido a parte, rotulado
 *    como tal, em avalanche-modo.csv.
 *  - Nonce fixo dentro de cada par (cifrarBlocoUnico). Com nonces
 *    diferentes as subchaves mudam e a diferenca de ~50% viria do nonce,
 *    nao do bit virado.
 *  - Chave, nonce, bloco e bit novos a cada par: a media cobre muitas
 *    S-boxes e permutacoes, e nao a sorte de uma chave.
 *  - Mesma semente para todas as cifras e rodadas: as linhas do CSV se
 *    comparam sobre exatamente os mesmos pares.
 *  - Semente fixa: o CSV e evidencia e tem de sair identico a cada execucao.
 *
 * Nenhuma assercao de qualidade (regra 2 do brief): se a avalanche der 31%,
 * o CSV mostra 31% e o relatorio explica. As assercoes abaixo so verificam
 * que a medicao mediu o que diz.
 */
class AtaqueDiferencialTest {

    private static final long SEMENTE = 0x2BL;
    private static final int PARES_POR_LINHA = 5_000;
    private static final int PARES_MODO = 2_000;
    private static final int RODADAS_MAX = 10;

    private enum Tipo { texto, chave }

    /** As tres cifras de bloco comparadas, com a mesma interface de medicao. */
    private interface CifraDeBloco {
        byte[] cifrar(byte[] chave, byte[] nonce, byte[] bloco, int rodadas);
    }

    private static CifraDeBloco sigea(CamadaDifusao camada, ModoPermutacao permutacao) {
        return (chave, nonce, bloco, rodadas) -> new CifraSigea(chave, rodadas, camada, permutacao).cifrarBlocoUnico(bloco, nonce);
    }

    private static final CifraDeBloco AES_256 = (chave, nonce, bloco, rodadas) -> AesReferencia.cifrarBloco(chave, bloco, rodadas);

    private static Estatistica.Resumo medir(CifraDeBloco cifra, int rodadas, Tipo tipo) {
        Random rnd = new Random(SEMENTE);
        Estatistica.Resumo resumo = new Estatistica.Resumo(128);
        for (int n = 0; n < PARES_POR_LINHA; n++) {
            byte[] chave = new byte[32];
            byte[] nonce = new byte[12];
            byte[] bloco = new byte[16];
            rnd.nextBytes(chave);
            rnd.nextBytes(nonce);
            rnd.nextBytes(bloco);
            int bitTexto = rnd.nextInt(128);
            int bitChave = rnd.nextInt(256);

            byte[] a = cifra.cifrar(chave, nonce, bloco, rodadas);
            byte[] b;
            if (tipo == Tipo.texto) {
                byte[] vizinho = bloco.clone();
                Estatistica.virarBit(vizinho, bitTexto);
                b = cifra.cifrar(chave, nonce, vizinho, rodadas);
            } else {
                byte[] outraChave = chave.clone();
                Estatistica.virarBit(outraChave, bitChave);
                b = cifra.cifrar(outraChave, nonce, bloco, rodadas);
            }
            resumo.adicionar(Estatistica.distanciaHamming(a, b));
        }
        return resumo;
    }

    @Test
    @DisplayName("Avalanche de texto e de chave por rodada: CifraSigea (atual, Adendo 01, Fase 1) x AES-256")
    void avalanchePorRodada() {
        Evidencias.Csv csv = new Evidencias.Csv("avalanche.csv",
                "cifra,rodadas,tipo,media_pct,desvio_pct,min_pct,max_pct,min_bits,max_bits,pares");
        Evidencias.Csv histograma = new Evidencias.Csv("avalanche-histograma.csv",
                "cifra,rodadas,tipo,bits_alterados,ocorrencias");

        Object[][] cifras = {
                // A cifra atual e as duas versoes anteriores, para o relatorio
                // mostrar o efeito de cada correcao sobre os mesmos pares:
                // Fase 1 (cadeia dupla, P livre) -> Adendo 01 (MixColumns,
                // P livre) -> atual (MixColumns, P espalhando colunas).
                {"CIFRA_SIGEA_MDS", sigea(CamadaDifusao.MIXCOLUMNS_MDS, ModoPermutacao.ESPALHA_COLUNAS), CifraSigea.RODADAS_PADRAO},
                {"CIFRA_SIGEA_MDS_P_LIVRE", sigea(CamadaDifusao.MIXCOLUMNS_MDS, ModoPermutacao.LIVRE), CifraSigea.RODADAS_PADRAO},
                {"CIFRA_SIGEA_CADEIA_DUPLA_P_LIVRE", sigea(CamadaDifusao.CADEIA_DUPLA, ModoPermutacao.LIVRE), CifraSigea.RODADAS_PADRAO},
                {"AES_256", AES_256, AesReferencia.RODADAS_AES_256},
        };
        for (Object[] c : cifras) {
            String nome = (String) c[0];
            CifraDeBloco cifra = (CifraDeBloco) c[1];
            int rodadasCompletas = (int) c[2];
            for (Tipo tipo : Tipo.values()) {
                int ultimaRodada = Math.max(RODADAS_MAX, rodadasCompletas);
                for (int r = 1; r <= ultimaRodada; r++) {
                    // De 1 a 10 para todas; o AES ganha tambem a linha das 14 rodadas reais.
                    if (r > RODADAS_MAX && r != rodadasCompletas) {
                        continue;
                    }
                    Estatistica.Resumo s = medir(cifra, r, tipo);
                    csv.linha("%s,%d,%s,%.4f,%.4f,%.4f,%.4f,%d,%d,%d", nome, r, tipo,
                            s.pct(s.media()), s.pct(s.desvio()), s.pct(s.min()), s.pct(s.max()), s.min(), s.max(), s.n());
                    if (r == rodadasCompletas) {
                        long[] h = s.histograma();
                        for (int bits = 0; bits < h.length; bits++) {
                            histograma.linha("%s,%d,%s,%d,%d", nome, r, tipo, bits, h[bits]);
                        }
                    }
                    assertEquals(PARES_POR_LINHA, s.n());
                }
            }
        }
        csv.gravar();
        histograma.gravar();
        // 4 cifras x 2 tipos x 10 rodadas, mais a linha de 14 rodadas do AES por tipo.
        assertEquals(4 * 2 * RODADAS_MAX + 2, csv.linhasDeDados());
    }

    /**
     * Propriedade do MODO de operacao, nao da cifra - rotulada a parte para
     * nao contaminar avalanche.csv.
     *
     * CifraSigea em CBC, mensagem de 10 blocos (160 bytes, que o PKCS#7
     * estende para 11): bit virado no primeiro bloco contamina tudo dali para
     * frente; no ultimo, so os dois blocos finais. AES-256-GCM e um modo de
     * fluxo (CTR): virar um bit do texto claro vira exatamente o mesmo bit do
     * ciphertext, e toda a "avalanche" fica na tag. Isso nao e fraqueza do
     * GCM: a integridade vem da tag, nao da difusao do corpo.
     */
    @Test
    @DisplayName("Avalanche do modo de operacao: CBC da CifraSigea x GCM do AES (item separado)")
    void avalancheDoModo() {
        Evidencias.Csv csv = new Evidencias.Csv("avalanche-modo.csv",
                "cifra,modo,cenario,bits_comparados,media_pct,desvio_pct,min_bits,max_bits,pares");
        int blocos = 10;
        int tamanho = blocos * 16;

        Estatistica.Resumo cbcPrimeiro = new Estatistica.Resumo((tamanho + 16) * 8);
        Estatistica.Resumo cbcUltimo = new Estatistica.Resumo((tamanho + 16) * 8);
        Estatistica.Resumo gcmCorpo = new Estatistica.Resumo(tamanho * 8);
        Estatistica.Resumo gcmTag = new Estatistica.Resumo(128);

        Random rnd = new Random(SEMENTE);
        for (int n = 0; n < PARES_MODO; n++) {
            byte[] chave = new byte[32];
            byte[] nonce = new byte[12];
            byte[] texto = new byte[tamanho];
            rnd.nextBytes(chave);
            rnd.nextBytes(nonce);
            rnd.nextBytes(texto);
            int bitPrimeiro = rnd.nextInt(128);
            int bitUltimo = (blocos - 1) * 128 + rnd.nextInt(128);

            CifraSigea cifra = new CifraSigea(chave);
            byte[] base = cifra.cifrarComNonce(texto, nonce).textoCifrado();
            byte[] v1 = texto.clone();
            Estatistica.virarBit(v1, bitPrimeiro);
            cbcPrimeiro.adicionar(Estatistica.distanciaHamming(base, cifra.cifrarComNonce(v1, nonce).textoCifrado()));
            byte[] v2 = texto.clone();
            Estatistica.virarBit(v2, bitUltimo);
            cbcUltimo.adicionar(Estatistica.distanciaHamming(base, cifra.cifrarComNonce(v2, nonce).textoCifrado()));

            byte[] g1 = AesReferencia.gcmCifrar(chave, nonce, texto);
            byte[] g2 = AesReferencia.gcmCifrar(chave, nonce, v1);
            gcmCorpo.adicionar(Estatistica.distanciaHamming(
                    java.util.Arrays.copyOf(g1, tamanho), java.util.Arrays.copyOf(g2, tamanho)));
            gcmTag.adicionar(Estatistica.distanciaHamming(
                    java.util.Arrays.copyOfRange(g1, tamanho, tamanho + 16), java.util.Arrays.copyOfRange(g2, tamanho, tamanho + 16)));
        }

        Object[][] linhas = {
                {"CIFRA_SIGEA_MDS", "CBC", "bit_no_primeiro_de_10_blocos", cbcPrimeiro},
                {"CIFRA_SIGEA_MDS", "CBC", "bit_no_ultimo_de_10_blocos", cbcUltimo},
                {"AES_256", "GCM", "corpo_bit_no_primeiro_bloco", gcmCorpo},
                {"AES_256", "GCM", "tag_bit_no_primeiro_bloco", gcmTag},
        };
        for (Object[] l : linhas) {
            Estatistica.Resumo s = (Estatistica.Resumo) l[3];
            csv.linha("%s,%s,%s,%d,%.4f,%.4f,%d,%d,%d", l[0], l[1], l[2], s.bitsTotal(),
                    s.pct(s.media()), s.pct(s.desvio()), s.min(), s.max(), s.n());
        }
        csv.gravar();
        // O GCM e CTR: um bit virado no texto claro vira exatamente um bit do corpo.
        assertEquals(1, gcmCorpo.min());
        assertEquals(1, gcmCorpo.max());
    }
}
