package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Medicao da secao 9 do Adendo 01: CADEIA_DUPLA x MIXCOLUMNS_MDS, 20.000
 * pares, 10 rodadas, bloco unico, contando os pares "ruins" (menos de 32
 * dos 128 bits alterados) por byte em que o bit foi virado.
 *
 * Bloco unico e nao mensagem CBC: o CBC so propaga para frente, e um bit
 * virado no ultimo bloco nao toca os anteriores - a media mediria o modo de
 * operacao e o tamanho da mensagem, nao a funcao de rodada.
 *
 * As duas camadas recebem exatamente os mesmos pares (mesma chave, nonce,
 * bloco e bit), entao a diferenca entre as linhas do CSV e so da camada.
 * Semente fixa: o CSV e evidencia de relatorio e precisa sair identico a
 * cada execucao. Este teste nao afirma nenhum numero "bom" - ele registra
 * o que medir; o julgamento fica no relatorio.
 */
class DiagnosticoDifusaoTest {

    private static final long SEMENTE = 20_000L;
    private static final int PARES = 20_000;
    private static final int RODADAS = 10;
    private static final int LIMIAR_RUIM_BITS = 32;

    static Path diretorioEvidencias() throws IOException {
        Path dir = Paths.get(System.getProperty("evidencias.dir", "../evidencias/projeto-2"));
        Files.createDirectories(dir);
        return dir;
    }

    @Test
    @DisplayName("Diagnostico da camada de difusao: 20.000 pares, 10 rodadas, bloco unico, por byte")
    void compararCamadas() throws IOException {
        CamadaDifusao[] camadas = CamadaDifusao.values();
        int k = camadas.length;
        long[] soma = new long[k];
        long[] somaQuadrados = new long[k];
        int[] min = new int[k];
        int[] max = new int[k];
        int[] ruins = new int[k];
        int[][] ruinsPorByte = new int[k][16];
        int[] paresPorByte = new int[16];
        java.util.Arrays.fill(min, 128);

        Random rnd = new Random(SEMENTE);
        for (int n = 0; n < PARES; n++) {
            byte[] chave = new byte[CifraSigea.TAMANHO_CHAVE];
            rnd.nextBytes(chave);
            byte[] nonce = new byte[CifraSigea.TAMANHO_NONCE];
            rnd.nextBytes(nonce);
            byte[] bloco = new byte[CifraSigea.TAMANHO_BLOCO];
            rnd.nextBytes(bloco);
            int bit = rnd.nextInt(128);
            byte[] vizinho = bloco.clone();
            vizinho[bit / 8] ^= (byte) (1 << (bit % 8));
            paresPorByte[bit / 8]++;

            for (int c = 0; c < k; c++) {
                CifraSigea cifra = new CifraSigea(chave, RODADAS, camadas[c]);
                int d = distanciaHamming(cifra.cifrarBlocoUnico(bloco, nonce), cifra.cifrarBlocoUnico(vizinho, nonce));
                soma[c] += d;
                somaQuadrados[c] += (long) d * d;
                min[c] = Math.min(min[c], d);
                max[c] = Math.max(max[c], d);
                if (d < LIMIAR_RUIM_BITS) {
                    ruins[c]++;
                    ruinsPorByte[c][bit / 8]++;
                }
            }
        }

        List<String> resumo = new ArrayList<>();
        resumo.add("camada,rodadas,pares,media_pct,desvio_pct,min_bits,max_bits,pares_abaixo_32_bits,pct_abaixo_32_bits");
        List<String> porByte = new ArrayList<>();
        porByte.add("camada,byte,pares,pares_abaixo_32_bits");
        for (int c = 0; c < k; c++) {
            double media = (double) soma[c] / PARES;
            double desvio = Math.sqrt((double) somaQuadrados[c] / PARES - media * media);
            resumo.add(String.format(Locale.ROOT, "%s,%d,%d,%.4f,%.4f,%d,%d,%d,%.4f",
                    camadas[c], RODADAS, PARES, 100 * media / 128, 100 * desvio / 128,
                    min[c], max[c], ruins[c], 100.0 * ruins[c] / PARES));
            for (int b = 0; b < 16; b++) {
                porByte.add(String.format(Locale.ROOT, "%s,%d,%d,%d", camadas[c], b, paresPorByte[b], ruinsPorByte[c][b]));
            }
            System.out.println("[DiagnosticoDifusao] " + resumo.get(resumo.size() - 1));
            System.out.println("[DiagnosticoDifusao] " + camadas[c] + " ruins por byte: " + java.util.Arrays.toString(ruinsPorByte[c]));
        }

        Path dir = diretorioEvidencias();
        Files.write(dir.resolve("difusao-comparacao.csv"), resumo, StandardCharsets.UTF_8);
        Files.write(dir.resolve("difusao-ruins-por-byte.csv"), porByte, StandardCharsets.UTF_8);

        // So consistencia interna da medicao, nenhum alvo de qualidade.
        for (int c = 0; c < k; c++) {
            assertEquals(ruins[c], java.util.Arrays.stream(ruinsPorByte[c]).sum());
        }
        assertEquals(PARES, java.util.Arrays.stream(paresPorByte).sum());
        assertTrue(Files.size(dir.resolve("difusao-comparacao.csv")) > 0);
    }

    static int distanciaHamming(byte[] a, byte[] b) {
        int d = 0;
        for (int i = 0; i < a.length; i++) {
            d += Integer.bitCount((a[i] ^ b[i]) & 0xFF);
        }
        return d;
    }
}
