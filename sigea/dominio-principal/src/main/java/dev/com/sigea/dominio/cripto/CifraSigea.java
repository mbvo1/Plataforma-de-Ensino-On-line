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
 *   3. Difusao      CamadaDifusao selecionada: MixColumns do Rijndael
 *                   (padrao) ou a cadeia dupla original do grupo
 *   4. Permute      saida[i] = rotl(entrada[P[i]], (i % 7) + 1), com P
 *                   sorteada pela chave de forma a espalhar cada coluna
 *                   pelas quatro (padrao; ver ModoPermutacao)
 *
 * Por que existe o passo 3: SubBytes e Permute trabalham byte a byte, e
 * sem uma camada linear que misture bytes entre si a mudanca de um bit
 * fica presa num so byte por varias rodadas - nao remover "para
 * simplificar". Por que a camada padrao deixou de ser a cadeia dupla do
 * brief e passou a ser a MixColumns do Rijndael: a cadeia tem uma posicao
 * (byte 14) em que a diferenca se cancela e sobra um unico byte afetado
 * na rodada; o diagnostico completo esta em CamadaDifusao.
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
 *   - Nao ha AddRoundKey final (o AES tem um). As tres ultimas operacoes
 *     (SubBytes, difusao, Permute) dependem so da chave mestra, nao do
 *     nonce: formam uma camada final fixa por chave, que um ataque de texto
 *     claro escolhido pode tentar remover. Segundo a referencia do Adendo
 *     01, acrescenta-lo nao muda a avalanche de forma mensuravel, entao a
 *     ausencia e fraqueza de seguranca, nao de difusao.
 *   - A validacao do padding nao e de tempo constante.
 *   - Nenhuma analise formal da S-box gerada (ver CaixaSubstituicao).
 *     O Adendo 01 suspeitava dela pela fracao de pares com difusao fraca
 *     que sobrava com a MixColumns; a medicao (DiagnosticoCaudaTest) mostrou
 *     que a causa era a permutacao LIVRE, nao a S-box - corrigida pelo
 *     modo ESPALHA_COLUNAS. A ausencia de analise da S-box continua valendo
 *     como fraqueza, so deixou de ser a explicacao daquela cauda.
 *
 * Imutavel e segura para uso concorrente: as tabelas da chave sao
 * calculadas no construtor e o estado de cada cifragem e local a chamada.
 */
public final class CifraSigea {

    public static final int TAMANHO_BLOCO = 16;
    public static final int TAMANHO_CHAVE = 32;
    public static final int TAMANHO_NONCE = 12;
    public static final int RODADAS_PADRAO = 10;
    public static final CamadaDifusao DIFUSAO_PADRAO = CamadaDifusao.MIXCOLUMNS_MDS;
    public static final ModoPermutacao PERMUTACAO_PADRAO = ModoPermutacao.ESPALHA_COLUNAS;

    // SecureRandom e thread-safe; uma instancia compartilhada evita
    // reabrir a fonte de entropia do SO a cada mensagem.
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final byte[] chaveMestra;
    private final int rodadas;
    private final CamadaDifusao difusao;
    private final ModoPermutacao permutacao;
    private final CaixaSubstituicao caixa;

    public CifraSigea(byte[] chaveMestra) {
        this(chaveMestra, RODADAS_PADRAO, DIFUSAO_PADRAO);
    }

    public CifraSigea(byte[] chaveMestra, int rodadas) {
        this(chaveMestra, rodadas, DIFUSAO_PADRAO);
    }

    /**
     * Rodadas e camada de difusao como parametros de construtor, nao
     * constantes: o experimento de avalanche por rodada (1..10) precisa
     * instanciar a mesma cifra com contagens diferentes para mostrar onde a
     * difusao satura, e a comparacao CADEIA_DUPLA x MIXCOLUMNS_MDS precisa
     * das duas camadas lado a lado sob a mesma chave.
     */
    public CifraSigea(byte[] chaveMestra, int rodadas, CamadaDifusao difusao) {
        this(chaveMestra, rodadas, difusao, PERMUTACAO_PADRAO);
    }

    /**
     * Com o modo de sorteio da permutacao P explicito. LIVRE so existe para
     * reproduzir a cifra da Fase 1 e do Adendo 01 e medir o antes e o depois;
     * ver ModoPermutacao.
     */
    public CifraSigea(byte[] chaveMestra, int rodadas, CamadaDifusao difusao, ModoPermutacao permutacao) {
        if (chaveMestra == null || chaveMestra.length != TAMANHO_CHAVE) {
            throw new IllegalArgumentException("Chave mestra deve ter exatamente " + TAMANHO_CHAVE + " bytes (256 bits)");
        }
        if (rodadas < 1) {
            throw new IllegalArgumentException("Numero de rodadas deve ser ao menos 1");
        }
        if (difusao == null) {
            throw new IllegalArgumentException("Camada de difusao nao pode ser nula");
        }
        if (permutacao == null) {
            throw new IllegalArgumentException("Modo de permutacao nao pode ser nulo");
        }
        // Copia defensiva: se o chamador zerar ou reutilizar o array, a cifra
        // nao passa a cifrar com outra chave sem ninguem perceber.
        this.chaveMestra = chaveMestra.clone();
        this.rodadas = rodadas;
        this.difusao = difusao;
        this.permutacao = permutacao;
        this.caixa = new CaixaSubstituicao(this.chaveMestra, TAMANHO_BLOCO, permutacao);
    }

    public int rodadas() {
        return rodadas;
    }

    public CamadaDifusao difusao() {
        return difusao;
    }

    public ModoPermutacao permutacao() {
        return permutacao;
    }

    /** Cifra com um nonce novo de 12 bytes do SecureRandom - o unico caminho de uso normal. */
    public ResultadoCifragem cifrar(byte[] textoClaro) {
        byte[] nonce = new byte[TAMANHO_NONCE];
        ALEATORIO.nextBytes(nonce);
        return cifrarComNonce(textoClaro, nonce);
    }

    /**
     * Cifra com um nonce escolhido pelo chamador. FIXAR O NONCE ANULA A
     * SEGURANCA DA CIFRA; o metodo existe so para medicao.
     *
     * Medir avalanche ou repetir uma cifragem para comparar exige fixar o
     * nonce, senao o nonce aleatorio sozinho ja produz ~50% de bits
     * diferentes e o numero nao diria nada sobre a cifra. Fora disso,
     * repetir nonce com a mesma chave repete subchaves e IV, e mensagens com
     * o mesmo prefixo passam a produzir o mesmo prefixo cifrado.
     *
     * Visibilidade de pacote de proposito (Adendo 01): publico, qualquer
     * modulo que dependa do dominio poderia chama-lo em producao. Por isso os
     * testes de ataque ficam neste mesmo pacote, nao num subpacote - em Java,
     * subpacote nao herda acesso de pacote.
     */
    ResultadoCifragem cifrarComNonce(byte[] textoClaro, byte[] nonce) {
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

    /**
     * Uma aplicacao da funcao de bloco, sem CBC, IV nem padding.
     *
     * Existe so para medicao: avalanche tem de ser medida na funcao de
     * rodada, porque o CBC so propaga para frente - um bit virado no ultimo
     * bloco de uma mensagem longa nao toca os anteriores, e a media passaria
     * a descrever o modo de operacao e o tamanho da mensagem, nao a cifra.
     * Visibilidade de pacote de proposito; nao e API de uso.
     */
    byte[] cifrarBlocoUnico(byte[] bloco, byte[] nonce) {
        if (bloco == null || bloco.length != TAMANHO_BLOCO) {
            throw new IllegalArgumentException("Bloco deve ter exatamente " + TAMANHO_BLOCO + " bytes");
        }
        validarNonce(nonce);
        byte[] s = bloco.clone();
        cifrarBloco(s, new EscalonadorChaves(chaveMestra, nonce, rodadas), new byte[TAMANHO_BLOCO]);
        return s;
    }

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
            // 3. Difusao
            difusao.aplicar(s);
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
     *   - Difusao: cada camada tem a sua inversa (a da cadeia dupla inverte
     *     a direcao dos lacos; a da MixColumns usa outra matriz) - ver
     *     CamadaDifusao.
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
            // 3^-1. Difusao
            difusao.inverter(s);
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
        return "CifraSigea[" + rodadas + " rodadas, " + difusao + ", " + permutacao + "]";
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
