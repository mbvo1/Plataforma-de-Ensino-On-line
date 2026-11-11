package dev.com.sigea.dominio.cripto;

/**
 * Passo 3 da rodada da CifraSigea: a camada linear de difusao. Selecionavel
 * por construtor, como as rodadas, porque as duas variantes sao objeto de
 * comparacao no relatorio.
 *
 * Historia da escolha (Adendo 01 ao brief):
 *
 * CADEIA_DUPLA foi a camada projetada pelo grupo - XOR encadeado para
 * frente e depois para tras. A media de avalanche saiu correta (~50%), mas
 * a cauda nao: cerca de 1 par em 200 terminava com pouquissimos bits
 * alterados, quase todos com o bit virado no byte 14 e nenhum em byte
 * impar. A causa e estrutural. Depois da passada para frente, f[i] e o
 * XOR de a[0..i]; depois da passada para tras, b[i] e o XOR de f[i..15].
 * Uma diferenca em a[j] sobrevive em b[i] quando aparece um numero impar
 * de vezes nesse XOR, o que, para j par, so acontece nas posicoes impares
 * i > j. Para j = 14 resta uma unica posicao (b[15]) na rodada inteira.
 * Como a permutacao de posicoes e fixa para a chave, a posicao fraca cai
 * sempre no mesmo lugar e o deficit atravessa as dez rodadas em vez de se
 * diluir. Variantes triangulares (ciclica, rotacionar e repetir) mantem o
 * defeito; por isso a troca foi de camada, nao de ajuste.
 *
 * MIXCOLUMNS_MDS e a MixColumns do Rijndael (AES, FIPS-197), adotada e nao
 * inventada aqui. Ela trata o bloco como quatro colunas de 4 bytes e
 * multiplica cada coluna por uma matriz MDS sobre GF(2^8). MDS significa
 * numero de ramificacao 5: bytes ativos na entrada + bytes ativos na saida
 * de uma coluna somam pelo menos 5, entao um byte diferente ativa os 4 da
 * coluna - igualmente para todas as posicoes, que e exatamente a garantia
 * que faltava a cadeia. A troca entre colunas fica a cargo do Permute
 * (permutacao derivada da chave), no papel que o ShiftRows tem no AES.
 *
 * A cifra continua propria na composicao: S-box dependente de chave,
 * escalonamento com nonce, permutacao derivada da chave e rotacoes.
 */
public enum CamadaDifusao {

    /** Camada original do grupo. Mantida so para comparacao; tem fraqueza estrutural documentada acima. */
    CADEIA_DUPLA {
        @Override
        void aplicar(byte[] s) {
            for (int i = 1; i < s.length; i++) {
                s[i] ^= s[i - 1];
            }
            for (int i = s.length - 2; i >= 0; i--) {
                s[i] ^= s[i + 1];
            }
        }

        /*
         * A inversa da passada para tras e um laco CRESCENTE (b[i] ^= b[i+1]
         * com b[i+1] ainda intacto) e a da passada para frente e DECRESCENTE.
         * Repetir as passadas na mesma direcao da cifragem nao as desfaz.
         */
        @Override
        void inverter(byte[] s) {
            for (int i = 0; i <= s.length - 2; i++) {
                s[i] ^= s[i + 1];
            }
            for (int i = s.length - 1; i >= 1; i--) {
                s[i] ^= s[i - 1];
            }
        }
    },

    /** MixColumns do Rijndael. Padrao desde o Adendo 01. */
    MIXCOLUMNS_MDS {
        @Override
        void aplicar(byte[] s) {
            for (int c = 0; c < s.length; c += 4) {
                misturarColuna(s, c, DIRETA);
            }
        }

        @Override
        void inverter(byte[] s) {
            for (int c = 0; c < s.length; c += 4) {
                misturarColuna(s, c, INVERSA);
            }
        }
    };

    // Matriz circulante do Rijndael e sua inversa (FIPS-197, secoes 5.1.3 e 5.3.3).
    private static final int[][] DIRETA = {
            {2, 3, 1, 1},
            {1, 2, 3, 1},
            {1, 1, 2, 3},
            {3, 1, 1, 2}
    };
    private static final int[][] INVERSA = {
            {14, 11, 13, 9},
            {9, 14, 11, 13},
            {13, 9, 14, 11},
            {11, 13, 9, 14}
    };

    abstract void aplicar(byte[] s);

    abstract void inverter(byte[] s);

    private static void misturarColuna(byte[] s, int inicio, int[][] matriz) {
        int a0 = s[inicio] & 0xFF;
        int a1 = s[inicio + 1] & 0xFF;
        int a2 = s[inicio + 2] & 0xFF;
        int a3 = s[inicio + 3] & 0xFF;
        for (int linha = 0; linha < 4; linha++) {
            int[] m = matriz[linha];
            s[inicio + linha] = (byte) (mul(m[0], a0) ^ mul(m[1], a1) ^ mul(m[2], a2) ^ mul(m[3], a3));
        }
    }

    /**
     * Multiplicacao em GF(2^8) com o polinomio redutor x^8+x^4+x^3+x+1 (0x11b),
     * o mesmo do AES - com outro polinomio a matriz deixaria de ser MDS e a
     * inversa acima deixaria de ser a inversa. Laco de "shift and add" em vez
     * de tabelas: o custo nao importa para objeto de estudo, e o codigo fica
     * verificavel a olho contra o FIPS-197.
     */
    static int mul(int a, int b) {
        int produto = 0;
        while (b != 0) {
            if ((b & 1) != 0) {
                produto ^= a;
            }
            a <<= 1;
            if ((a & 0x100) != 0) {
                a ^= 0x11b;
            }
            b >>>= 1;
        }
        return produto & 0xFF;
    }
}
