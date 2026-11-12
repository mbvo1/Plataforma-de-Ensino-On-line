package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 2a - forca bruta.
 *
 * O numero 2^256 sozinho nao demonstra nada. O que demonstra e a curva:
 * busca exaustiva REAL com a chave truncada em 8, 12, 16, 20 e 24 bits
 * desconhecidos (o atacante conhece os outros bits), cronometrada, e o
 * ajuste exponencial dessa curva extrapolado para 256 bits.
 *
 * Decisoes de metodo:
 *  - Oraculo: um par texto claro / texto cifrado conhecido (ataque de texto
 *    claro conhecido), com o nonce publico, como esta no formato v1:.
 *  - Varredura completa do espaco, nao "ate achar". Parar ao achar daria
 *    um numero de tentativas aleatorio (media 2^(n-1)) e uma curva
 *    serrilhada; varrer tudo mede o custo de pior caso, que e o que se
 *    extrapola, e ainda prova que so UMA chave do espaco satisfaz o oraculo
 *    (sem falso positivo). A posicao da chave certa vai em coluna separada.
 *  - Uma thread. A taxa medida e por nucleo; atacante real paraleliza, e
 *    o relatorio deve dizer isso em vez de esconder num numero agregado.
 *  - Cada candidata da CifraSigea paga a geracao da S-box e da permutacao
 *    (16 rodadas de Fisher-Yates com SHA-256), porque elas dependem da chave.
 *    Isso deixa a cifra propria mais lenta POR CHAVE que o AES - o que nao e
 *    seguranca: e um fator constante, que some diante de 2^256.
 *  - AES-256-GCM pelo JCE (com AES-NI quando a CPU tem), com init por
 *    candidata, que inclui a expansao de chave e o calculo de H do GHASH.
 *
 * O teste longo e marcado como "experimento" e fica fora do mvn test
 * padrao (24 bits leva minutos). Rodar com:
 *   mvn test -pl dominio-principal -Pexperimentos
 */
class AtaqueForcaBrutaTest {

    private static final long SEMENTE = 0xB0A7L;
    private static final int[] BITS = {8, 12, 16, 20, 24};
    private static final double TAXA_HIPOTETICA = 1e12;
    private static final double SEGUNDOS_POR_ANO = 365.25 * 24 * 3600;

    // 15 bytes: com o PKCS#7, exatamente um bloco cifrado na CifraSigea.
    private static final byte[] TEXTO_CONHECIDO = "Aula cancelada.".getBytes(StandardCharsets.US_ASCII);

    /** Testa uma chave candidata contra o par conhecido. */
    private interface Oraculo {
        boolean confere(byte[] candidata);
    }

    private static final class Cenario {
        final byte[] chaveSecreta;
        final byte[] nonce;

        Cenario(long semente) {
            Random rnd = new Random(semente);
            chaveSecreta = new byte[32];
            nonce = new byte[12];
            rnd.nextBytes(chaveSecreta);
            rnd.nextBytes(nonce);
        }

        Oraculo oraculoSigea() {
            byte[] alvo = new CifraSigea(chaveSecreta).cifrarComNonce(TEXTO_CONHECIDO, nonce).textoCifrado();
            assertEquals(16, alvo.length);
            return candidata -> Arrays.equals(alvo, new CifraSigea(candidata).cifrarComNonce(TEXTO_CONHECIDO, nonce).textoCifrado());
        }

        Oraculo oraculoAes() {
            byte[] alvo = AesReferencia.gcmCifrar(chaveSecreta, nonce, TEXTO_CONHECIDO);
            GCMParameterSpec parametros = new GCMParameterSpec(128, nonce);
            Cipher cipher;
            try {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
            return candidata -> {
                try {
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(candidata, "AES"), parametros);
                    return Arrays.equals(alvo, cipher.doFinal(TEXTO_CONHECIDO));
                } catch (GeneralSecurityException e) {
                    throw new IllegalStateException(e);
                }
            };
        }
    }

    private static final class ResultadoBusca {
        long tentativas;
        long tentativasAteAchar = -1;
        int chavesEncontradas;
        double segundos;
    }

    /**
     * Varre os 2^bits valores dos bits baixos da chave, mantendo os altos
     * (conhecidos) iguais aos da chave secreta.
     */
    private static ResultadoBusca buscar(Cenario cenario, Oraculo oraculo, int bits) {
        return buscar(cenario, oraculo, bits, null, null);
    }

    /**
     * Com {@code ritmo} preenchido, grava tempo e coleta de lixo a cada 2^20
     * candidatas. Existe porque a primeira execucao completa mostrou a taxa da
     * CifraSigea caindo pela metade de 20 para 24 bits; sem o ritmo por trecho
     * nao da para separar a causa (GC da cifra, que aloca tabelas novas por
     * candidata, ou a maquina sob carga prolongada).
     */
    private static ResultadoBusca buscar(Cenario cenario, Oraculo oraculo, int bits, String nome, Evidencias.Csv ritmo) {
        final long trecho = 1L << 20;
        long inicioTrecho = System.nanoTime();
        long gcInicioTrecho = tempoGcMs();
        byte[] base = cenario.chaveSecreta.clone();
        // Zera os bits desconhecidos; a candidata e base | valor.
        for (int b = 0; b < bits; b++) {
            base[31 - b / 8] &= (byte) ~(1 << (b % 8));
        }
        byte[] candidata = base.clone();
        ResultadoBusca r = new ResultadoBusca();
        long total = 1L << bits;
        long inicio = System.nanoTime();
        for (long v = 0; v < total; v++) {
            candidata[31] = (byte) (base[31] | (v & 0xFF));
            candidata[30] = (byte) (base[30] | ((v >>> 8) & 0xFF));
            candidata[29] = (byte) (base[29] | ((v >>> 16) & 0xFF));
            if (oraculo.confere(candidata)) {
                r.chavesEncontradas++;
                if (r.tentativasAteAchar < 0) {
                    r.tentativasAteAchar = v + 1;
                }
                assertArrayEquals(cenario.chaveSecreta, candidata, "o oraculo aceitou uma chave diferente da secreta");
            }
            if (ritmo != null && (v + 1) % trecho == 0) {
                long agora = System.nanoTime();
                long gcAgora = tempoGcMs();
                double segundosTrecho = (agora - inicioTrecho) / 1e9;
                ritmo.linha("%s,%d,%d,%.4f,%.1f,%d", nome, bits, (v + 1) / trecho, segundosTrecho,
                        trecho / segundosTrecho, gcAgora - gcInicioTrecho);
                inicioTrecho = agora;
                gcInicioTrecho = gcAgora;
            }
        }
        r.segundos = (System.nanoTime() - inicio) / 1e9;
        r.tentativas = total;
        return r;
    }

    private static long tempoGcMs() {
        long total = 0;
        for (java.lang.management.GarbageCollectorMXBean gc : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            total += Math.max(0, gc.getCollectionTime());
        }
        return total;
    }

    private static void aquecer(Oraculo oraculo) {
        // Sem aquecimento, os primeiros milhares de chamadas rodam
        // interpretados e o ponto de 8 bits sai artificialmente lento.
        byte[] k = new byte[32];
        for (int i = 0; i < 20_000; i++) {
            k[0] = (byte) i;
            k[1] = (byte) (i >>> 8);
            oraculo.confere(k);
        }
    }

    @Test
    @DisplayName("Busca exaustiva de 8 bits acha exatamente a chave secreta, nas duas cifras")
    void buscaPequenaAchaAChave() {
        Cenario cenario = new Cenario(SEMENTE);
        for (Oraculo oraculo : new Oraculo[]{cenario.oraculoSigea(), cenario.oraculoAes()}) {
            ResultadoBusca r = buscar(cenario, oraculo, 8);
            assertEquals(1, r.chavesEncontradas);
            assertEquals(256, r.tentativas);
        }
    }

    @Test
    @Tag("experimento")
    @DisplayName("Forca bruta medida: 8 a 24 bits nas duas cifras, extrapolada para 2^256")
    void medirForcaBruta() {
        Cenario cenario = new Cenario(SEMENTE);
        Evidencias.Csv csv = new Evidencias.Csv("forca-bruta.csv",
                "cifra,bits_chave,tentativas,segundos,chaves_por_segundo,tentativas_ate_achar,chaves_encontradas");
        Evidencias.Csv extrapolacao = new Evidencias.Csv("forca-bruta-extrapolacao.csv",
                "cifra,inclinacao_log2_segundos_por_bit,intercepto_log2_segundos,r2,chaves_por_segundo_24_bits,"
                        + "espaco_2_256,segundos_2_256_taxa_medida,anos_2_256_taxa_medida,"
                        + "segundos_2_256_a_1e12_por_s,anos_2_256_a_1e12_por_s");

        Evidencias.Csv ritmo = new Evidencias.Csv("forca-bruta-ritmo.csv",
                "cifra,bits_chave,trecho_de_2_20,segundos,chaves_por_segundo,gc_ms");

        BigDecimal espaco = new BigDecimal(BigInteger.ONE.shiftLeft(256));
        Object[][] cifras = {
                {"CIFRA_SIGEA_MDS", cenario.oraculoSigea()},
                {"AES_256_GCM", cenario.oraculoAes()},
        };
        for (Object[] c : cifras) {
            String nome = (String) c[0];
            Oraculo oraculo = (Oraculo) c[1];
            aquecer(oraculo);
            double[] x = new double[BITS.length];
            double[] y = new double[BITS.length];
            double taxa24 = 0;
            for (int i = 0; i < BITS.length; i++) {
                ResultadoBusca r = buscar(cenario, oraculo, BITS[i], nome, ritmo);
                assertEquals(1, r.chavesEncontradas, nome + " " + BITS[i] + " bits");
                double taxa = r.tentativas / r.segundos;
                csv.linha("%s,%d,%d,%.6f,%.1f,%d,%d", nome, BITS[i], r.tentativas, r.segundos, taxa,
                        r.tentativasAteAchar, r.chavesEncontradas);
                System.out.printf("[ForcaBruta] %s %d bits: %.3f s%n", nome, BITS[i], r.segundos);
                x[i] = BITS[i];
                y[i] = Math.log(r.segundos) / Math.log(2);
                taxa24 = taxa;
            }
            // Minimos quadrados de log2(segundos) contra bits: inclinacao perto
            // de 1 confirma que cada bit a mais dobra o tempo.
            double[] ajuste = regressaoLinear(x, y);
            BigDecimal seg = espaco.divide(BigDecimal.valueOf(taxa24), MathContext.DECIMAL64);
            BigDecimal seg12 = espaco.divide(BigDecimal.valueOf(TAXA_HIPOTETICA), MathContext.DECIMAL64);
            BigDecimal ano = BigDecimal.valueOf(SEGUNDOS_POR_ANO);
            extrapolacao.linha("%s,%.4f,%.4f,%.6f,%.1f,%.6e,%.6e,%.6e,%.6e,%.6e", nome, ajuste[0], ajuste[1], ajuste[2], taxa24,
                    espaco, seg, seg.divide(ano, MathContext.DECIMAL64), seg12, seg12.divide(ano, MathContext.DECIMAL64));
        }
        csv.gravar();
        extrapolacao.gravar();
        ritmo.gravar();
    }

    /** Devolve {inclinacao, intercepto, r2}. */
    static double[] regressaoLinear(double[] x, double[] y) {
        int n = x.length;
        double mx = Arrays.stream(x).average().orElse(0);
        double my = Arrays.stream(y).average().orElse(0);
        double sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < n; i++) {
            sxy += (x[i] - mx) * (y[i] - my);
            sxx += (x[i] - mx) * (x[i] - mx);
            syy += (y[i] - my) * (y[i] - my);
        }
        double a = sxy / sxx;
        return new double[]{a, my - a * mx, (sxy * sxy) / (sxx * syy)};
    }
}
