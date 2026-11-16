package dev.com.sigea.dominio.cripto;

import java.util.HashSet;
import java.util.Set;

/** Metricas usadas pelos ataques da Fase 2. Sem dependencia externa, para que cada formula fique a vista. */
final class Estatistica {

    private Estatistica() {
    }

    static int distanciaHamming(byte[] a, byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Tamanhos diferentes: " + a.length + " x " + b.length);
        }
        int d = 0;
        for (int i = 0; i < a.length; i++) {
            d += Integer.bitCount((a[i] ^ b[i]) & 0xFF);
        }
        return d;
    }

    static void virarBit(byte[] dados, int bit) {
        dados[bit / 8] ^= (byte) (1 << (bit % 8));
    }

    /**
     * Media, desvio padrao populacional, minimo e maximo de uma serie de
     * distancias em bits, mais o histograma - acumulados em uma passada.
     */
    static final class Resumo {
        private final int bitsTotal;
        private final long[] histograma;
        private long n;
        private long soma;
        private long somaQuadrados;
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;

        Resumo(int bitsTotal) {
            this.bitsTotal = bitsTotal;
            this.histograma = new long[bitsTotal + 1];
        }

        void adicionar(int bits) {
            n++;
            soma += bits;
            somaQuadrados += (long) bits * bits;
            min = Math.min(min, bits);
            max = Math.max(max, bits);
            histograma[bits]++;
        }

        long n() { return n; }
        int bitsTotal() { return bitsTotal; }
        int min() { return min; }
        int max() { return max; }
        long[] histograma() { return histograma.clone(); }

        double media() {
            return (double) soma / n;
        }

        double desvio() {
            double m = media();
            return Math.sqrt(Math.max(0, (double) somaQuadrados / n - m * m));
        }

        double pct(double bits) {
            return 100.0 * bits / bitsTotal;
        }
    }

    static long[] frequencias(byte[] dados) {
        long[] f = new long[256];
        for (byte b : dados) {
            f[b & 0xFF]++;
        }
        return f;
    }

    /** Qui-quadrado contra a distribuicao uniforme sobre os 256 valores de byte (255 graus de liberdade). */
    static double quiQuadradoUniforme(long[] freq) {
        long total = 0;
        for (long f : freq) total += f;
        double esperado = total / 256.0;
        double x2 = 0;
        for (long f : freq) {
            double d = f - esperado;
            x2 += d * d / esperado;
        }
        return x2;
    }

    /**
     * P(X >= x2) para qui-quadrado com k graus de liberdade, pela
     * aproximacao de Wilson-Hilferty: (X/k)^(1/3) e aproximadamente normal
     * com media 1 - 2/(9k) e variancia 2/(9k). Com k = 255 o erro e muito
     * menor que a casa decimal que importa aqui (rejeitar ou nao a 5%), e
     * evita implementar a gama incompleta so para isso.
     */
    static double pValorQuiQuadrado(double x2, int k) {
        double v = 2.0 / (9.0 * k);
        double z = (Math.cbrt(x2 / k) - (1 - v)) / Math.sqrt(v);
        return 0.5 * erfc(z / Math.sqrt(2));
    }

    /** erfc por Abramowitz e Stegun 7.1.26 (erro absoluto < 1,5e-7), estendida para x negativo. */
    static double erfc(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double poly = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741 + t * (-1.453152027 + t * 1.061405429))));
        double r = poly * Math.exp(-x * x);
        return x >= 0 ? r : 2 - r;
    }

    /** Entropia de Shannon em bits por byte; 8 e o maximo, atingido so pela distribuicao uniforme. */
    static double entropia(long[] freq) {
        long total = 0;
        for (long f : freq) total += f;
        double h = 0;
        for (long f : freq) {
            if (f > 0) {
                double p = (double) f / total;
                h -= p * (Math.log(p) / Math.log(2));
            }
        }
        return h;
    }

    /**
     * Indice de coincidencia: probabilidade de dois bytes sorteados sem
     * reposicao serem iguais. Uniforme sobre 256 valores da 1/256 = 0,0039;
     * lingua natural fica bem acima, porque poucas letras dominam.
     */
    static double indiceCoincidencia(long[] freq) {
        long total = 0;
        double soma = 0;
        for (long f : freq) {
            total += f;
            soma += (double) f * (f - 1);
        }
        return soma / ((double) total * (total - 1));
    }

    /** Blocos de 16 bytes que repetem um bloco ja visto (total - distintos). */
    static int blocosRepetidos(byte[] dados) {
        Set<String> vistos = new HashSet<>();
        int repetidos = 0;
        for (int off = 0; off + 16 <= dados.length; off += 16) {
            String bloco = java.util.HexFormat.of().formatHex(dados, off, off + 16);
            if (!vistos.add(bloco)) {
                repetidos++;
            }
        }
        return repetidos;
    }
}
