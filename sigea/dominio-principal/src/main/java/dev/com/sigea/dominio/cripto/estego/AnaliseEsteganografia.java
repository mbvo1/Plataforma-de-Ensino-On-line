package dev.com.sigea.dominio.cripto.estego;

/**
 * Metricas da Fase 3: quanto o embutimento altera a imagem (PSNR) e se ele e
 * detectavel (ataque qui-quadrado sobre o plano LSB).
 *
 * Moram no codigo principal, e nao so nos testes, porque sao o "detector"
 * que o brief pede e o dashboard da Fase 6 pode reusa-las; leem os pixels
 * pelo mesmo caminho que EsteganografiaLSB usa para embutir.
 */
public final class AnaliseEsteganografia {

    private static final int CANAIS = 3;

    /**
     * Pares (2k, 2k+1) com menos que isto somado ficam de fora do
     * qui-quadrado. A aproximacao qui-quadrado so vale com frequencia
     * esperada de pelo menos ~5 por celula; abaixo disso uma unica contagem
     * pequena domina a soma. A frequencia esperada de cada celula do par e
     * (a + b) / 2, entao o corte e a + b >= 10. Imagens de tela, com poucas
     * cores, ficam com poucos pares validos - e o motivo de usar a gama
     * incompleta exata e nao uma aproximacao para muitos graus de liberdade.
     */
    static final int MINIMO_POR_PAR = 10;

    private AnaliseEsteganografia() {
    }

    /**
     * PSNR em dB entre duas imagens do mesmo tamanho, sobre R, G e B. O alfa
     * fica de fora porque o embutimento nunca o toca; incluir um canal sempre
     * igual so diluiria o erro medio. Imagens identicas: +infinito.
     */
    public static double psnr(int[] originalArgb, int[] alteradaArgb) {
        if (originalArgb.length != alteradaArgb.length) {
            throw new IllegalArgumentException("Imagens de tamanhos diferentes");
        }
        long somaQuadrados = 0;
        for (int i = 0; i < originalArgb.length; i++) {
            for (int canal = 0; canal < CANAIS; canal++) {
                int d = canal(originalArgb[i], canal) - canal(alteradaArgb[i], canal);
                somaQuadrados += (long) d * d;
            }
        }
        if (somaQuadrados == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double mse = (double) somaQuadrados / ((long) originalArgb.length * CANAIS);
        return 10 * Math.log10(255.0 * 255.0 / mse);
    }

    /** Resultado do ataque sobre um trecho: estatistica, graus de liberdade e valor-p. */
    public record ResultadoQuiQuadrado(long valoresAnalisados, double quiQuadrado, int grausLiberdade, double pValor) {
    }

    /**
     * Ataque qui-quadrado de Westfeld e Pfitzmann ("Attacks on
     * Steganographic Systems", Information Hiding 1999) sobre os primeiros
     * valores de canal, na ordem de percurso do embutimento.
     *
     * Por que funciona: trocar o LSB leva o valor 2k para 2k+1 e vice-versa,
     * nunca para fora do par. Com um payload que parece aleatorio (um
     * ciphertext), metade dos valores de cada par migra para o outro, e as
     * contagens h[2k] e h[2k+1] tendem a ficar iguais. Numa imagem natural ou
     * de tela elas sao, em geral, bem diferentes.
     *
     * Estatistica: soma sobre os pares validos de (h[2k] - m)^2 / m, com
     * m = (h[2k] + h[2k+1]) / 2, e (pares - 1) graus de liberdade. O valor
     * devolvido e a probabilidade de uma estatistica ao menos tao grande sob a
     * hipotese "contagens iguais por par" - perto de 1, os pares estao
     * igualados e ha embutimento; perto de 0, a imagem parece limpa. Note a
     * leitura invertida em relacao ao teste de uniformidade da Fase 2.
     *
     * @param pontos quantos trechos medir; o trecho i cobre os primeiros
     *               i/pontos dos valores de canal. O histograma e acumulado
     *               uma vez so, entao a curva inteira custa uma passada.
     */
    public static ResultadoQuiQuadrado[] curvaQuiQuadrado(int[] argb, int pontos) {
        if (pontos < 1) {
            throw new IllegalArgumentException("Ao menos um ponto");
        }
        long totalValores = (long) argb.length * CANAIS;
        long[] histograma = new long[256];
        ResultadoQuiQuadrado[] curva = new ResultadoQuiQuadrado[pontos];
        long lidos = 0;
        for (int p = 1; p <= pontos; p++) {
            long ate = totalValores * p / pontos;
            for (; lidos < ate; lidos++) {
                histograma[canal(argb[(int) (lidos / CANAIS)], (int) (lidos % CANAIS))]++;
            }
            curva[p - 1] = avaliar(histograma, lidos);
        }
        return curva;
    }

    /** O ataque sobre a imagem inteira. */
    public static ResultadoQuiQuadrado quiQuadrado(int[] argb) {
        return curvaQuiQuadrado(argb, 1)[0];
    }

    /** O ataque sobre os primeiros {@code valores} valores de canal. */
    public static ResultadoQuiQuadrado quiQuadrado(int[] argb, long valores) {
        long total = (long) argb.length * CANAIS;
        if (valores < 0 || valores > total) {
            throw new IllegalArgumentException("Trecho fora da imagem: " + valores + " de " + total);
        }
        long[] histograma = new long[256];
        for (long i = 0; i < valores; i++) {
            histograma[canal(argb[(int) (i / CANAIS)], (int) (i % CANAIS))]++;
        }
        return avaliar(histograma, valores);
    }

    private static ResultadoQuiQuadrado avaliar(long[] h, long valores) {
        double x2 = 0;
        int pares = 0;
        for (int k = 0; k < 128; k++) {
            long a = h[2 * k];
            long b = h[2 * k + 1];
            if (a + b < MINIMO_POR_PAR) {
                continue;
            }
            double m = (a + b) / 2.0;
            x2 += (a - m) * (a - m) / m;
            pares++;
        }
        int graus = pares - 1;
        // Com menos de 2 pares validos nao ha teste; NaN em vez de um numero inventado.
        double p = graus < 1 ? Double.NaN : gamaRegularizadaSuperior(graus / 2.0, x2 / 2.0);
        return new ResultadoQuiQuadrado(valores, x2, Math.max(graus, 0), p);
    }

    /** Valor do canal 0 = R, 1 = G, 2 = B, mesma ordem de EsteganografiaLSB. */
    static int canal(int argb, int canal) {
        return (argb >>> EsteganografiaLSB.deslocamentoDoCanal(canal)) & 0xFF;
    }

    // ------------------------------------------------------------------
    // Funcao gama incompleta regularizada Q(a, x) = Gamma(a, x) / Gamma(a)
    // A cauda superior da qui-quadrado com k graus e Q(k/2, x/2). Serie para
    // x < a + 1 e fracao continuada (Lentz) no resto - o par classico, cada
    // um no lado em que converge rapido.
    // ------------------------------------------------------------------

    static double gamaRegularizadaSuperior(double a, double x) {
        if (a <= 0) {
            throw new IllegalArgumentException("a deve ser positivo");
        }
        if (x <= 0) {
            return 1.0;
        }
        if (x < a + 1) {
            return 1.0 - serieInferior(a, x);
        }
        return fracaoContinuadaSuperior(a, x);
    }

    private static double serieInferior(double a, double x) {
        double termo = 1.0 / a;
        double soma = termo;
        for (int n = 1; n < 10_000; n++) {
            termo *= x / (a + n);
            soma += termo;
            if (Math.abs(termo) < Math.abs(soma) * 1e-15) {
                break;
            }
        }
        return soma * Math.exp(-x + a * Math.log(x) - lnGama(a));
    }

    private static double fracaoContinuadaSuperior(double a, double x) {
        final double minusculo = 1e-300;
        double b = x + 1 - a;
        double c = 1 / minusculo;
        double d = 1 / b;
        double h = d;
        for (int i = 1; i < 10_000; i++) {
            double an = -i * (i - a);
            b += 2;
            d = an * d + b;
            if (Math.abs(d) < minusculo) d = minusculo;
            c = b + an / c;
            if (Math.abs(c) < minusculo) c = minusculo;
            d = 1 / d;
            double delta = d * c;
            h *= delta;
            if (Math.abs(delta - 1) < 1e-15) {
                break;
            }
        }
        return Math.exp(-x + a * Math.log(x) - lnGama(a)) * h;
    }

    /** ln Gamma(x) pela aproximacao de Lanczos (g = 7, 9 coeficientes), erro ~1e-15. */
    static double lnGama(double x) {
        final double[] c = {0.99999999999980993, 676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6,
                1.5056327351493116e-7};
        if (x < 0.5) {
            return Math.log(Math.PI / Math.abs(Math.sin(Math.PI * x))) - lnGama(1 - x);
        }
        x -= 1;
        double soma = c[0];
        for (int i = 1; i < 9; i++) {
            soma += c[i] / (x + i);
        }
        double t = x + 7.5;
        return 0.5 * Math.log(2 * Math.PI) + (x + 0.5) * Math.log(t) - t + Math.log(soma);
    }
}
