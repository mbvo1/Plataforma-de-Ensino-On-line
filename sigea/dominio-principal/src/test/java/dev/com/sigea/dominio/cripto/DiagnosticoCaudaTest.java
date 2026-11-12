package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Investigacao da cauda residual da MIXCOLUMNS_MDS: mesmo com a camada MDS,
 * uma fracao de pares termina com bem menos de 32 dos 128 bits alterados,
 * muito acima do ideal binomial. Regra 2 do brief: relatar e investigar,
 * nao ajustar ate o numero ficar bonito.
 *
 * Duas hipoteses, medidas sobre os mesmos 20.000 pares de
 * DiagnosticoDifusaoTest (mesma semente):
 *
 * H1 (Adendo 01) - a S-box gerada por hash da chave, sem garantia de
 * nao-linearidade, tem diferenciais mais provaveis. Metrica: uniformidade
 * diferencial da S-box daquela chave (maior entrada da tabela de
 * distribuicao de diferencas; o AES tem 4, permutacoes aleatorias ficam
 * tipicamente entre 10 e 12).
 *
 * H2 - a MixColumns so mistura bytes DENTRO de cada coluna de 4 bytes; quem
 * leva bytes de uma coluna para outra e so o Permute, cuja permutacao P e
 * sorteada pela chave. Se P levar todos os bytes de um conjunto de colunas
 * para dentro do mesmo conjunto, uma diferenca nascida ali nunca sai dele -
 * em nenhuma rodada, porque P e o mesmo em todas. Com uma coluna fechada, a
 * diferenca fica presa em 32 bits e o par e "ruim" quase sempre. O AES evita
 * isso por construcao: o ShiftRows e fixo e manda cada coluna para as
 * quatro. Metrica: tamanho do menor conjunto de colunas fechado sob P que
 * contem a coluna do bit virado (4 = nenhum confinamento).
 *
 * Previsao de H2, antes de medir: a chance de uma coluna especifica ser
 * fechada e 4!12!/16! = 1/1820; so os pares com o bit nessa coluna sofrem;
 * logo ~4 x (1/1820) x (1/4) = 0,055% dos pares - contra 0,06% observados.
 */
class DiagnosticoCaudaTest {

    private static final long SEMENTE = 20_000L;
    private static final int PARES = 20_000;
    private static final int RODADAS = 10;
    private static final int LIMIAR_RUIM_BITS = 32;

    /** Menor conjunto de colunas fechado sob o Permute que contem {@code coluna}; 4 se so o bloco inteiro. */
    static int menorConjuntoFechado(int[] origemDaPosicao, int coluna) {
        // Permute: saida[i] = entrada[P[i]], entao o byte da posicao P[i] vai para i.
        int[] destino = new int[16];
        for (int i = 0; i < 16; i++) {
            destino[origemDaPosicao[i]] = i;
        }
        int menor = 4;
        for (int conjunto = 1; conjunto < 15; conjunto++) {
            if ((conjunto & (1 << coluna)) == 0) {
                continue;
            }
            boolean fechado = true;
            for (int pos = 0; pos < 16 && fechado; pos++) {
                if ((conjunto & (1 << (pos / 4))) != 0 && (conjunto & (1 << (destino[pos] / 4))) == 0) {
                    fechado = false;
                }
            }
            if (fechado) {
                menor = Math.min(menor, Integer.bitCount(conjunto));
            }
        }
        return menor;
    }

    static int uniformidadeDiferencial(byte[] sbox) {
        int maior = 0;
        int[] contagem = new int[256];
        for (int dx = 1; dx < 256; dx++) {
            java.util.Arrays.fill(contagem, 0);
            for (int x = 0; x < 256; x++) {
                int dy = (sbox[x] ^ sbox[x ^ dx]) & 0xFF;
                if (++contagem[dy] > maior) {
                    maior = contagem[dy];
                }
            }
        }
        return maior;
    }

    @Test
    @DisplayName("Cauda da MIXCOLUMNS_MDS: S-box (H1) x colunas fechadas pela permutacao (H2)")
    void investigarCauda() {
        // [tamanho do conjunto fechado 1..4] -> pares, ruins, soma de bits
        long[][] porConfinamento = new long[5][3];
        // [uniformidade diferencial 0..32] -> pares, ruins
        long[][] porUniformidade = new long[33][2];

        Evidencias.Csv ruins = new Evidencias.Csv("cauda-pares-ruins.csv",
                "par,bits_alterados,byte_virado,coluna,menor_conjunto_fechado,uniformidade_diferencial_sbox");

        Random rnd = new Random(SEMENTE);
        for (int n = 0; n < PARES; n++) {
            // Mesma ordem de sorteio de DiagnosticoDifusaoTest: os pares sao os mesmos.
            byte[] chave = new byte[32];
            rnd.nextBytes(chave);
            byte[] nonce = new byte[12];
            rnd.nextBytes(nonce);
            byte[] bloco = new byte[16];
            rnd.nextBytes(bloco);
            int bit = rnd.nextInt(128);
            byte[] vizinho = bloco.clone();
            Estatistica.virarBit(vizinho, bit);

            CifraSigea cifra = new CifraSigea(chave, RODADAS, CamadaDifusao.MIXCOLUMNS_MDS);
            int d = Estatistica.distanciaHamming(cifra.cifrarBlocoUnico(bloco, nonce), cifra.cifrarBlocoUnico(vizinho, nonce));

            // Mesmas tabelas que a cifra usou: CaixaSubstituicao e deterministica na chave.
            CaixaSubstituicao caixa = new CaixaSubstituicao(chave, 16);
            int byteVirado = bit / 8;
            int confinamento = menorConjuntoFechado(caixa.permutacaoPosicoes(), byteVirado / 4);
            int uniformidade = uniformidadeDiferencial(caixa.sbox());

            boolean ruim = d < LIMIAR_RUIM_BITS;
            porConfinamento[confinamento][0]++;
            porConfinamento[confinamento][2] += d;
            porUniformidade[uniformidade][0]++;
            if (ruim) {
                porConfinamento[confinamento][1]++;
                porUniformidade[uniformidade][1]++;
                ruins.linha("%d,%d,%d,%d,%d,%d", n, d, byteVirado, byteVirado / 4, confinamento, uniformidade);
            }
        }

        Evidencias.Csv resumo = new Evidencias.Csv("cauda-diagnostico.csv",
                "hipotese,grupo,pares,pares_abaixo_32_bits,pct_abaixo_32_bits,media_pct");
        long total = 0;
        for (int k = 1; k <= 4; k++) {
            long[] g = porConfinamento[k];
            total += g[0];
            if (g[0] > 0) {
                resumo.linha("H2_permutacao,menor_conjunto_fechado_%d_colunas,%d,%d,%.4f,%.4f", k, g[0], g[1],
                        100.0 * g[1] / g[0], 100.0 * g[2] / g[0] / 128);
            }
        }
        for (int u = 0; u < porUniformidade.length; u++) {
            long[] g = porUniformidade[u];
            if (g[0] > 0) {
                resumo.linha("H1_sbox,uniformidade_diferencial_%d,%d,%d,%.4f,", u, g[0], g[1], 100.0 * g[1] / g[0]);
            }
        }
        resumo.gravar();
        ruins.gravar();
        assertEquals(PARES, total);
    }
}
