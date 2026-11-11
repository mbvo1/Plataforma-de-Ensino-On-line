package dev.com.sigea.dominio.cripto;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tabelas da CifraSigea que dependem so da chave mestra: a S-box de 256
 * entradas (com a inversa) e a permutacao das 16 posicoes do bloco.
 *
 * Por que as duas moram aqui e nao no EscalonadorChaves: o criterio da
 * divisao e o que cada tabela depende. Estas dependem apenas da chave
 * mestra, entao sao calculadas uma vez na construcao da cifra e reusadas
 * em todas as mensagens. O EscalonadorChaves cuida do que depende do nonce
 * e, portanto, muda a cada mensagem.
 *
 * Por que S-box dependente de chave em vez de uma tabela fixa como a do
 * AES: e o mecanismo que faz a chave secreta influenciar o passo de
 * confusao, nao so o XOR do AddRoundKey. O preco, que fica registrado
 * como fraqueza para a Parte 4, e que ninguem analisou estas tabelas:
 * a S-box do AES foi escolhida por propriedades algebricas provadas
 * (nao-linearidade, uniformidade diferencial); uma permutacao aleatoria
 * e boa "em media", mas uma chave especifica pode gerar uma S-box fraca
 * e nao ha como garantir o contrario sem testar cada chave.
 *
 * Fluxo de aleatoriedade: SHA-256(chaveMestra || contador), contador de
 * 4 bytes big-endian a partir de 0. A S-box consome o fluxo primeiro e a
 * permutacao de posicoes continua do ponto onde a S-box parou - um unico
 * fluxo evita inventar um segundo rotulo de dominio fora da especificacao.
 * A entrada tem 36 bytes, enquanto a das subchaves de rodada tem 48
 * (chave || nonce || r), entao os dois usos do SHA-256 nunca recebem a
 * mesma mensagem.
 */
public final class CaixaSubstituicao {

    private static final int TAMANHO_SBOX = 256;

    private final byte[] sbox;
    private final byte[] sboxInversa;
    private final int[] permutacaoPosicoes;

    public CaixaSubstituicao(byte[] chaveMestra, int tamanhoBloco) {
        FluxoChave fluxo = new FluxoChave(chaveMestra);

        int[] permutacaoSbox = fisherYates(TAMANHO_SBOX, fluxo);
        this.sbox = new byte[TAMANHO_SBOX];
        this.sboxInversa = new byte[TAMANHO_SBOX];
        for (int i = 0; i < TAMANHO_SBOX; i++) {
            sbox[i] = (byte) permutacaoSbox[i];
            // A inversa sai de graca da propria permutacao; a decifragem
            // nao tem outro jeito de desfazer o SubBytes.
            sboxInversa[permutacaoSbox[i]] = (byte) i;
        }

        this.permutacaoPosicoes = fisherYates(tamanhoBloco, fluxo);
    }

    public int substituir(int valor) {
        return sbox[valor & 0xFF] & 0xFF;
    }

    public int substituirInverso(int valor) {
        return sboxInversa[valor & 0xFF] & 0xFF;
    }

    /** Posicao de origem do byte que vai parar na posicao {@code destino} apos o Permute. */
    public int origemDaPosicao(int destino) {
        return permutacaoPosicoes[destino];
    }

    /** Copias, para os experimentos da Fase 2 inspecionarem as tabelas sem poder altera-las. */
    public byte[] sbox() {
        return sbox.clone();
    }

    public int[] permutacaoPosicoes() {
        return permutacaoPosicoes.clone();
    }

    /**
     * Fisher-Yates "de tras para frente". O indice sorteado usa amostragem
     * por rejeicao em vez de {@code byte % (i + 1)}: o modulo direto daria
     * mais peso aos valores baixos sempre que 256 nao for multiplo de i+1,
     * e uma S-box enviesada e justamente o tipo de defeito que o ataque
     * estatistico da Fase 2 procuraria.
     */
    private static int[] fisherYates(int n, FluxoChave fluxo) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int faixa = i + 1;
            int limite = 256 - (256 % faixa);
            int sorteado;
            do {
                sorteado = fluxo.proximoByte();
            } while (sorteado >= limite);
            int j = sorteado % faixa;
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        return p;
    }

    /** SHA-256 em modo contador sobre a chave mestra, consumido byte a byte. */
    private static final class FluxoChave {
        private final byte[] chaveMestra;
        private final MessageDigest sha256;
        private int contador = 0;
        private byte[] blocoAtual = new byte[0];
        private int posicao = 0;

        FluxoChave(byte[] chaveMestra) {
            this.chaveMestra = chaveMestra;
            try {
                this.sha256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
            }
        }

        int proximoByte() {
            if (posicao == blocoAtual.length) {
                sha256.reset();
                sha256.update(chaveMestra);
                sha256.update(ByteBuffer.allocate(Integer.BYTES).putInt(contador++).array());
                blocoAtual = sha256.digest();
                posicao = 0;
            }
            return blocoAtual[posicao++] & 0xFF;
        }
    }

    @Override
    public String toString() {
        // Nunca imprimir as tabelas: elas sao funcao direta da chave.
        return "CaixaSubstituicao[" + permutacaoPosicoes.length + " posicoes, sbox oculta]";
    }
}
