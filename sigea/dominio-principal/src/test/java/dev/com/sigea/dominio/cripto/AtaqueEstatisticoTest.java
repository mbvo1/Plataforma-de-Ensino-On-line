package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 2c - ataque estatistico.
 *
 * Se o ciphertext preserva alguma estatistica do texto claro, a analise de
 * frequencia recupera informacao sem a chave. Mede-se, sobre texto claro e
 * sobre os dois ciphertexts: qui-quadrado contra a uniforme (255 graus de
 * liberdade), entropia de Shannon (bits/byte, maximo 8) e indice de
 * coincidencia (uniforme = 1/256).
 *
 * Corpus: o brief pede avisos e topicos do seed do banco, mas esse seed nao
 * existe - nenhuma migration insere em Avisos ou Topicos. Por decisao do
 * grupo, o corpus e o portugues real escrito no proprio repositorio: todos os
 * arquivos .md, lidos na execucao (sem copia que possa envelhecer). Fica de
 * fora evidencias/projeto-2, para o relatorio desta fase nunca virar entrada
 * da propria medicao. Blocos de codigo cercados por ``` sao removidos, porque
 * a analise de frequencia so e informativa sobre lingua natural. Ressalva
 * para o relatorio: e prosa tecnica, nao aviso de turma. A lista de arquivos,
 * os tamanhos e o SHA-256 do corpus vao para corpus-estatistico.csv.
 *
 * Cada paragrafo e cifrado como uma mensagem separada, com nonce proprio,
 * como cada aviso seria gravado no banco. Mede-se o que iria para a coluna
 * menos o nonce (que e aleatorio por construcao e so diluiria o sinal):
 * corpo + padding na CifraSigea, corpo + tag no GCM.
 *
 * Teste de padrao repetitivo: 10 KB de "A". Com ECB, blocos iguais de
 * texto claro viram blocos iguais cifrados; com CBC e GCM, nao. A
 * CifraSigea nao tem modo ECB; o "ECB emulado" aplica a funcao de bloco a
 * cada bloco com o mesmo nonce, que e exatamente o que ECB faria com ela.
 *
 * Nonces sorteados de semente fixa (via cifrarComNonce): o CSV tem de sair
 * identico a cada execucao.
 */
class AtaqueEstatisticoTest {

    private static final long SEMENTE = 0xC0FFEEL;
    private static final int GRAUS_LIBERDADE = 255;

    private static final class Corpus {
        final List<String> arquivos = new ArrayList<>();
        final List<Integer> bytesPorArquivo = new ArrayList<>();
        final List<byte[]> mensagens = new ArrayList<>();

        byte[] concatenado() {
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            mensagens.forEach(saida::writeBytes);
            return saida.toByteArray();
        }
    }

    private static Corpus carregarCorpus() throws IOException {
        Path raiz = Paths.get(System.getProperty("corpus.raiz", "../..")).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(raiz.resolve("README.md")), "Raiz do repositorio nao encontrada em " + raiz);

        // git ls-files e nao uma varredura do disco: arquivo ignorado ou nao
        // versionado (relatorio gerado por ferramenta, rascunho local) mudaria
        // o corpus de maquina para maquina, e o CSV deixaria de ser reproduzivel.
        List<Path> arquivos = arquivosVersionados(raiz).stream()
                .filter(r -> r.toLowerCase().endsWith(".md"))
                .filter(r -> !foraDoCorpus(r))
                .sorted()
                .map(raiz::resolve)
                .collect(Collectors.toList());
        assertFalse(arquivos.isEmpty(), "Nenhum .md versionado encontrado em " + raiz);

        Corpus corpus = new Corpus();
        for (Path arquivo : arquivos) {
            String texto = removerBlocosDeCodigo(Files.readString(arquivo, StandardCharsets.UTF_8));
            int bytes = 0;
            for (String paragrafo : texto.split("\\R\\s*\\R")) {
                String limpo = paragrafo.strip();
                if (!limpo.isEmpty()) {
                    byte[] b = limpo.getBytes(StandardCharsets.UTF_8);
                    corpus.mensagens.add(b);
                    bytes += b.length;
                }
            }
            corpus.arquivos.add(relativo(raiz, arquivo));
            corpus.bytesPorArquivo.add(bytes);
        }
        return corpus;
    }

    private static String relativo(Path raiz, Path p) {
        return raiz.relativize(p).toString().replace('\\', '/');
    }

    private static List<String> arquivosVersionados(Path raiz) throws IOException {
        Process git = new ProcessBuilder("git", "ls-files", "-z").directory(raiz.toFile()).redirectErrorStream(true).start();
        String saida = new String(git.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int codigo = git.waitFor();
            assertEquals(0, codigo, "git ls-files falhou (git precisa estar no PATH): " + saida);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
        return Arrays.stream(saida.split("\0")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private static boolean foraDoCorpus(String relativo) {
        return relativo.contains("evidencias/projeto-2/");
    }

    private static String removerBlocosDeCodigo(String markdown) {
        StringBuilder saida = new StringBuilder();
        boolean dentro = false;
        for (String linha : markdown.split("\\R", -1)) {
            if (linha.strip().startsWith("```")) {
                dentro = !dentro;
                continue;
            }
            if (!dentro) {
                saida.append(linha).append('\n');
            }
        }
        return saida.toString();
    }

    private static void linhaEstatistica(Evidencias.Csv csv, String fonte, String modo, String entrada, byte[] dados) {
        long[] f = Estatistica.frequencias(dados);
        double x2 = Estatistica.quiQuadradoUniforme(f);
        csv.linha("%s,%s,%s,%d,%.4f,%d,%.6e,%.6f,%.8f,%d,%d", fonte, modo, entrada, dados.length, x2, GRAUS_LIBERDADE,
                Estatistica.pValorQuiQuadrado(x2, GRAUS_LIBERDADE), Estatistica.entropia(f),
                Estatistica.indiceCoincidencia(f), dados.length / 16, Estatistica.blocosRepetidos(dados));
    }

    @Test
    @DisplayName("Frequencia de bytes, qui-quadrado, entropia, IC e padrao repetitivo: CifraSigea x AES-256-GCM")
    void analiseEstatistica() throws IOException {
        Random rnd = new Random(SEMENTE);
        byte[] chave = new byte[32];
        rnd.nextBytes(chave);
        CifraSigea cifra = new CifraSigea(chave);

        // --- Corpus em portugues ---
        Corpus corpus = carregarCorpus();
        byte[] claro = corpus.concatenado();
        ByteArrayOutputStream sigea = new ByteArrayOutputStream();
        ByteArrayOutputStream aes = new ByteArrayOutputStream();
        for (byte[] mensagem : corpus.mensagens) {
            byte[] nonce = new byte[12];
            rnd.nextBytes(nonce);
            sigea.writeBytes(cifra.cifrarComNonce(mensagem, nonce).textoCifrado());
            aes.writeBytes(AesReferencia.gcmCifrar(chave, nonce, mensagem));
        }
        byte[] cifradoSigea = sigea.toByteArray();
        byte[] cifradoAes = aes.toByteArray();

        Evidencias.Csv estatistico = new Evidencias.Csv("estatistico.csv",
                "fonte,modo,entrada,bytes,qui_quadrado,graus_liberdade,p_valor,entropia_bits_por_byte,"
                        + "indice_coincidencia,blocos_16,blocos_repetidos");
        linhaEstatistica(estatistico, "TEXTO_CLARO", "-", "corpus_pt", claro);
        linhaEstatistica(estatistico, "CIFRA_SIGEA_MDS", "CBC", "corpus_pt", cifradoSigea);
        linhaEstatistica(estatistico, "AES_256", "GCM", "corpus_pt", cifradoAes);

        // --- 10 KB de "A" ---
        byte[] letraA = new byte[10 * 1024];
        Arrays.fill(letraA, (byte) 'A');
        byte[] nonce = new byte[12];
        rnd.nextBytes(nonce);
        byte[] sigeaCbc = cifra.cifrarComNonce(letraA, nonce).textoCifrado();
        byte[] sigeaEcb = new byte[letraA.length];
        for (int off = 0; off < letraA.length; off += 16) {
            System.arraycopy(cifra.cifrarBlocoUnico(Arrays.copyOfRange(letraA, off, off + 16), nonce), 0, sigeaEcb, off, 16);
        }
        byte[] aesGcm = AesReferencia.gcmCifrar(chave, nonce, letraA);
        byte[] aesEcb = AesReferencia.ecbCifrar(chave, letraA);

        linhaEstatistica(estatistico, "TEXTO_CLARO", "-", "10KB_letra_A", letraA);
        linhaEstatistica(estatistico, "CIFRA_SIGEA_MDS", "CBC", "10KB_letra_A", sigeaCbc);
        linhaEstatistica(estatistico, "CIFRA_SIGEA_MDS", "ECB_EMULADO", "10KB_letra_A", sigeaEcb);
        linhaEstatistica(estatistico, "AES_256", "GCM", "10KB_letra_A", aesGcm);
        linhaEstatistica(estatistico, "AES_256", "ECB", "10KB_letra_A", aesEcb);
        estatistico.gravar();

        // --- Histograma de bytes do corpus ---
        long[] fClaro = Estatistica.frequencias(claro);
        long[] fSigea = Estatistica.frequencias(cifradoSigea);
        long[] fAes = Estatistica.frequencias(cifradoAes);
        Evidencias.Csv frequencia = new Evidencias.Csv("frequencia-bytes.csv", "byte,texto_claro,cifra_sigea_mds_cbc,aes_256_gcm");
        for (int b = 0; b < 256; b++) {
            frequencia.linha("%d,%d,%d,%d", b, fClaro[b], fSigea[b], fAes[b]);
        }
        frequencia.gravar();

        // --- Rastreabilidade do corpus ---
        Evidencias.Csv corpusCsv = new Evidencias.Csv("corpus-estatistico.csv", "arquivo,bytes_texto");
        for (int i = 0; i < corpus.arquivos.size(); i++) {
            corpusCsv.linha("%s,%d", corpus.arquivos.get(i), corpus.bytesPorArquivo.get(i));
        }
        corpusCsv.linha("TOTAL_%d_MENSAGENS_SHA256_%s,%d", corpus.mensagens.size(), sha256(claro), claro.length);
        corpusCsv.gravar();

        // Propriedades do modo de operacao, deterministicas - nao metas de qualidade.
        assertEquals(0, Estatistica.blocosRepetidos(sigeaCbc), "CBC nao pode repetir bloco");
        assertEquals(0, Estatistica.blocosRepetidos(aesGcm), "GCM nao pode repetir bloco");
        assertEquals(letraA.length / 16 - 1, Estatistica.blocosRepetidos(sigeaEcb), "ECB de bloco constante repete todos menos o primeiro");
        assertEquals(letraA.length / 16 - 1, Estatistica.blocosRepetidos(aesEcb));
        assertTrue(claro.length > 16 * 1024, "corpus pequeno demais para 256 classes: " + claro.length + " bytes");
    }

    private static String sha256(byte[] dados) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
