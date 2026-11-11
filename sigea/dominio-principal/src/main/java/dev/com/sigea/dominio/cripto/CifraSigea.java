package dev.com.sigea.dominio.cripto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * CifraSigea: cifra de bloco propria, rede de substituicao e permutacao
 * (SPN) de 16 bytes, chave mestra de 256 bits, CBC com PKCS#7.
 *
 * E OBJETO DE ESTUDO, nao substituto do AES. Ela convive com o
 * AES-256-GCM do CpfCryptoConverter, que e o baseline de comparacao nos
 * ataques da Fase 2. Nada de producao deve depender dela para sigilo real.
 *
 * Rodada de cifragem, nesta ordem (a ordem e parte da especificacao):
 *   1. AddRoundKey  bloco[i] ^= K_r[i]          - onde a chave entra (XOR)
 *   2. SubBytes     bloco[i] = S[bloco[i]]       - confusao, S-box da chave
 *   3. MixChain     frente: i=1..15  b[i] ^= b[i-1]
 *                   tras:   i=14..0  b[i] ^= b[i+1]
 *   4. Permute      saida[i] = rotl(entrada[P[i]], (i % 7) + 1)
 *
 * Por que o MixChain tem as duas passadas: a passada para frente leva a
 * mudanca de um byte para todos os bytes a direita dele; a passada para
 * tras traz o resultado de volta para a esquerda. Depois das duas, um bit
 * alterado em qualquer posicao ja tocou os 16 bytes numa unica rodada.
 * Sem esse passo, a mudanca fica presa num so byte (SubBytes e Permute
 * sao byte a byte) e o avalanche nao chega perto de 50% - nao remover
 * "para simplificar".
 *
 * Por que a rotacao usa o indice de DESTINO (posicao depois da
 * permutacao): assim a quantidade de bits girados e fixa por posicao de
 * saida, e a inversa so precisa desgirar antes de despermutar, sem
 * consultar a permutacao para descobrir o deslocamento. A rotacao em
 * (i % 7) + 1 nunca e 0 nem 8, entao todo byte e de fato girado; isso
 * desalinha os bits entre rodadas, para que o mesmo bit de um byte nao
 * caia sempre na mesma posicao de entrada da S-box.
 *
 * Por que CBC e nao ECB: com ECB, blocos de texto claro iguais viram
 * blocos cifrados iguais e o padrao do texto vaza (a demonstracao de
 * 10 KB de "A" na Fase 2 mostra isso). O IV vem do EscalonadorChaves,
 * derivado de chave + nonce.
 *
 * FRAQUEZAS CONHECIDAS - documentadas para a Parte 4, NAO corrigir aqui:
 *   - Sem autenticacao. Diferente do GCM do CpfCryptoConverter, nao ha tag:
 *     um bit alterado no ciphertext nao e detectado como adulteracao (vira
 *     lixo num bloco e inversao de bit controlada no bloco seguinte - bit
 *     flipping do CBC). E a falha de padding gera uma excecao distinguivel das
 *     demais; quem expuser essa diferenca ao usuario cria um padding
 *     oracle (Vaudenay, 2002) que recupera o texto claro sem a chave.
 *   - A ultima rodada termina em MixChain + Permute, que nao envolvem
 *     subchave. Como nao ha AddRoundKey final (o AES tem um), o atacante
 *     que conhecer P consegue desfazer esse trecho; na pratica a ultima
 *     rodada protege menos que as outras.
 *   - A validacao do padding nao e de tempo constante.
 *   - Nenhuma analise formal da S-box gerada (ver CaixaSubstituicao).
 *
 * Imutavel e segura para uso concorrente: as tabelas da chave sao
 * calculadas no construtor e o estado de cada cifragem e local a chamada.
 */
public final class CifraSigea {

    public static final int TAMANHO_BLOCO = 16;
    public static final int TAMANHO_CHAVE = 32;
    public static final int TAMANHO_NONCE = 12;
    public static final int RODADAS_PADRAO = 10;

    // SecureRandom e thread-safe; uma instancia compartilhada evita
    // reabrir a fonte de entropia do SO a cada mensagem.
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final byte[] chaveMestra;
    private final int rodadas;
    private final CaixaSubstituicao caixa;

    public CifraSigea(byte[] chaveMestra) {
        this(chaveMestra, RODADAS_PADRAO);
    }

    /**
     * Rodadas como parametro de construtor, nao constante: o experimento de
     * avalanche por rodada (1..10) da Fase 2 precisa instanciar a mesma
     * cifra com contagens diferentes para mostrar onde a difusao satura.
     */
    public CifraSigea(byte[] chaveMestra, int rodadas) {
        if (chaveMestra == null || chaveMestra.length != TAMANHO_CHAVE) {
            throw new IllegalArgumentException("Chave mestra deve ter exatamente " + TAMANHO_CHAVE + " bytes (256 bits)");
        }
        if (rodadas < 1) {
            throw new IllegalArgumentException("Numero de rodadas deve ser ao menos 1");
        }
        // Copia defensiva: se o chamador zerar ou reutilizar o array, a cifra
        // nao passa a cifrar com outra chave sem ninguem perceber.
        this.chaveMestra = chaveMestra.clone();
        this.rodadas = rodadas;
        this.caixa = new CaixaSubstituicao(this.chaveMestra, TAMANHO_BLOCO);
    }

    public int rodadas() {
        return rodadas;
    }

    /** Cifra com um nonce novo de 12 bytes do SecureRandom - o unico caminho de uso normal. */
    public ResultadoCifragem cifrar(byte[] textoClaro) {
        byte[] nonce = new byte[TAMANHO_NONCE];
        ALEATORIO.nextBytes(nonce);
        return cifrarComNonce(textoClaro, nonce);
    }

    /**
     * Cifra com um nonce escolhido pelo chamador.
     *
     * Existe por causa dos experimentos da Fase 2: medir avalanche de texto
     * ou de chave exige fixar o nonce, senao o nonce aleatorio sozinho ja
     * produz ~50% de bits diferentes e o numero nao diria nada sobre a
     * cifra. NUNCA usar fora de experimento: repetir nonce com a mesma chave
     * repete subchaves e IV, e mensagens com o mesmo prefixo passam a
     * produzir o mesmo prefixo cifrado.
     */
    public ResultadoCifragem cifrarComNonce(byte[] textoClaro, byte[] nonce) {
        if (textoClaro == null) {
            throw new IllegalArgumentException("Texto claro nao pode ser nulo (use array vazio)");
        }
        validarNonce(nonce);
        long inicio = System.nanoTime();

        EscalonadorChaves escalonador = new EscalonadorChaves(chaveMestra, nonce, rodadas);
        byte[] dados = aplicarPadding(textoClaro);
        byte[] anterior = escalonador.iv().clone();
        byte[] bloco = new byte[TAMANHO_BLOCO];
        byte[] temp = new byte[TAMANHO_BLOCO];

        for (int off = 0; off < dados.length; off += TAMANHO_BLOCO) {
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                bloco[i] = (byte) (dados[off + i] ^ anterior[i]);
            }
            cifrarBloco(bloco, escalonador, temp);
            System.arraycopy(bloco, 0, dados, off, TAMANHO_BLOCO);
            System.arraycopy(bloco, 0, anterior, 0, TAMANHO_BLOCO);
        }

        long tempo = System.nanoTime() - inicio;
        return new ResultadoCifragem(nonce, dados, tempo);
    }

    public byte[] decifrar(ResultadoCifragem resultado) {
        return decifrar(resultado.nonce(), resultado.textoCifrado());
    }

    public byte[] decifrar(byte[] nonce, byte[] textoCifrado) {
        validarNonce(nonce);
        if (textoCifrado == null || textoCifrado.length == 0 || textoCifrado.length % TAMANHO_BLOCO != 0) {
            throw new FalhaDecifragem("Texto cifrado deve ter tamanho multiplo de " + TAMANHO_BLOCO + " e nao vazio");
        }

        EscalonadorChaves escalonador = new EscalonadorChaves(chaveMestra, nonce, rodadas);
        byte[] saida = new byte[textoCifrado.length];
        byte[] anterior = escalonador.iv().clone();
        byte[] bloco = new byte[TAMANHO_BLOCO];
        byte[] temp = new byte[TAMANHO_BLOCO];

        for (int off = 0; off < textoCifrado.length; off += TAMANHO_BLOCO) {
            System.arraycopy(textoCifrado, off, bloco, 0, TAMANHO_BLOCO);
            decifrarBloco(bloco, escalonador, temp);
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                saida[off + i] = (byte) (bloco[i] ^ anterior[i]);
            }
            System.arraycopy(textoCifrado, off, anterior, 0, TAMANHO_BLOCO);
        }

        return removerPadding(saida);
    }

    /** Texto UTF-8 para {@code v1:} + Base64(nonce || ciphertext), o formato das colunas TEXT. */
    public String cifrarTexto(String textoClaro) {
        if (textoClaro == null) {
            return null;
        }
        return cifrar(textoClaro.getBytes(StandardCharsets.UTF_8)).paraTextoVersionado();
    }

    public String decifrarTexto(String valorVersionado) {
        if (valorVersionado == null) {
            return null;
        }
        if (!valorVersionado.startsWith(ResultadoCifragem.PREFIXO_VERSAO)) {
            throw new FalhaDecifragem("Formato desconhecido: prefixo de versao ausente");
        }
        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(valorVersionado.substring(ResultadoCifragem.PREFIXO_VERSAO.length()));
        } catch (IllegalArgumentException e) {
            throw new FalhaDecifragem("Base64 invalido", e);
        }
        if (payload.length < TAMANHO_NONCE + TAMANHO_BLOCO) {
            throw new FalhaDecifragem("Payload curto demais para conter nonce e um bloco");
        }
        byte[] nonce = Arrays.copyOfRange(payload, 0, TAMANHO_NONCE);
        byte[] cifrado = Arrays.copyOfRange(payload, TAMANHO_NONCE, payload.length);
        return new String(decifrar(nonce, cifrado), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------
    // Bloco
    // ------------------------------------------------------------------

    private void cifrarBloco(byte[] s, EscalonadorChaves escalonador, byte[] temp) {
        for (int r = 1; r <= rodadas; r++) {
            byte[] k = escalonador.subchave(r);

            // 1. AddRoundKey
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                s[i] ^= k[i];
            }
            // 2. SubBytes
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                s[i] = (byte) caixa.substituir(s[i]);
            }
            // 3. MixChain - frente, depois tras; cada passo usa o vizinho ja atualizado.
            for (int i = 1; i < TAMANHO_BLOCO; i++) {
                s[i] ^= s[i - 1];
            }
            for (int i = TAMANHO_BLOCO - 2; i >= 0; i--) {
                s[i] ^= s[i + 1];
            }
            // 4. Permute - move os bytes e gira cada um pela posicao de destino.
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                temp[i] = rotacionarEsquerda(s[caixa.origemDaPosicao(i)], deslocamento(i));
            }
            System.arraycopy(temp, 0, s, 0, TAMANHO_BLOCO);
        }
    }

    /**
     * Inverso exato da rodada, passos na ordem inversa e rodadas de tras
     * para frente. Os dois pontos onde e facil errar:
     *   - MixChain: a inversa da passada para tras e um laco CRESCENTE
     *     (b[i] ^= b[i+1], com b[i+1] ainda intacto), e a inversa da
     *     passada para frente e um laco DECRESCENTE (b[i] ^= b[i-1], com
     *     b[i-1] ainda intacto). Repetir as passadas na mesma direcao da
     *     cifragem nao as desfaz, e o erro aparece so em alguns bytes.
     *   - Permute: desgirar ANTES de despermutar, porque a rotacao foi
     *     aplicada pela posicao de destino.
     * O teste de propriedade com 1.000 entradas existe para pegar isso.
     */
    private void decifrarBloco(byte[] s, EscalonadorChaves escalonador, byte[] temp) {
        for (int r = rodadas; r >= 1; r--) {
            byte[] k = escalonador.subchave(r);

            // 4^-1. Permute
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                temp[caixa.origemDaPosicao(i)] = rotacionarDireita(s[i], deslocamento(i));
            }
            System.arraycopy(temp, 0, s, 0, TAMANHO_BLOCO);
            // 3^-1. MixChain - desfaz a passada para tras, depois a para frente.
            for (int i = 0; i <= TAMANHO_BLOCO - 2; i++) {
                s[i] ^= s[i + 1];
            }
            for (int i = TAMANHO_BLOCO - 1; i >= 1; i--) {
                s[i] ^= s[i - 1];
            }
            // 2^-1. SubBytes
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                s[i] = (byte) caixa.substituirInverso(s[i]);
            }
            // 1^-1. AddRoundKey - XOR e a propria inversa.
            for (int i = 0; i < TAMANHO_BLOCO; i++) {
                s[i] ^= k[i];
            }
        }
    }

    private static int deslocamento(int posicao) {
        return (posicao % 7) + 1;
    }

    private static byte rotacionarEsquerda(byte valor, int bits) {
        int v = valor & 0xFF;
        return (byte) ((v << bits) | (v >>> (8 - bits)));
    }

    private static byte rotacionarDireita(byte valor, int bits) {
        int v = valor & 0xFF;
        return (byte) ((v >>> bits) | (v << (8 - bits)));
    }

    // ------------------------------------------------------------------
    // PKCS#7
    // ------------------------------------------------------------------

    /**
     * Sempre acrescenta de 1 a 16 bytes, inclusive quando o texto ja e
     * multiplo de 16 (vira um bloco inteiro de 0x10). Sem esse bloco extra,
     * um texto que terminasse em 0x01 seria indistinguivel de padding.
     */
    private static byte[] aplicarPadding(byte[] texto) {
        int preenchimento = TAMANHO_BLOCO - (texto.length % TAMANHO_BLOCO);
        byte[] saida = Arrays.copyOf(texto, texto.length + preenchimento);
        Arrays.fill(saida, texto.length, saida.length, (byte) preenchimento);
        return saida;
    }

    private static byte[] removerPadding(byte[] dados) {
        int preenchimento = dados[dados.length - 1] & 0xFF;
        if (preenchimento < 1 || preenchimento > TAMANHO_BLOCO) {
            throw new FalhaDecifragem("Padding PKCS#7 invalido");
        }
        for (int i = dados.length - preenchimento; i < dados.length; i++) {
            if ((dados[i] & 0xFF) != preenchimento) {
                throw new FalhaDecifragem("Padding PKCS#7 invalido");
            }
        }
        return Arrays.copyOf(dados, dados.length - preenchimento);
    }

    private static void validarNonce(byte[] nonce) {
        if (nonce == null || nonce.length != TAMANHO_NONCE) {
            throw new IllegalArgumentException("Nonce deve ter exatamente " + TAMANHO_NONCE + " bytes");
        }
    }

    @Override
    public String toString() {
        // Nunca incluir a chave: toString vai parar em log sem ninguem decidir isso.
        return "CifraSigea[" + rodadas + " rodadas]";
    }

    /**
     * Decifragem recusada: tamanho invalido, formato invalido ou padding
     * invalido. Chave errada costuma cair aqui pelo padding, mas nao sempre
     * (cerca de 1 em 256 vezes o ultimo byte decifrado e 0x01 por acaso e o
     * resultado e lixo sem excecao) - por isso nao existe "chave errada"
     * como diagnostico, e quem chama nunca deve tratar ausencia de excecao
     * como prova de autenticidade. Essa garantia so o GCM da.
     */
    public static final class FalhaDecifragem extends RuntimeException {
        public FalhaDecifragem(String mensagem) {
            super(mensagem);
        }

        public FalhaDecifragem(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }
}
