package dev.com.sigea.dominio.cripto;

import dev.com.sigea.dominio.cripto.estego.AnaliseEsteganografia;
import dev.com.sigea.dominio.cripto.estego.AnaliseEsteganografia.ResultadoQuiQuadrado;
import dev.com.sigea.dominio.cripto.estego.EsteganografiaLSB;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 3 - medicoes de esteganografia sobre capas reais: PSNR, capacidade
 * usada e o ataque qui-quadrado de Westfeld e Pfitzmann.
 *
 * Capas: todos os PNG versionados no repositorio (git ls-files), sem
 * duplicatas por SHA-256 - a regra escolhe as imagens, nao quem mede.
 * Ressalva para o relatorio: sao todas capturas de tela (o repositorio nao
 * tem fotografias). Tela tem grandes areas de cor chapada e histograma em
 * picos, o caso mais facil para o detector; fotografia, com ruido de
 * sensor, tende a esconder melhor. A lista vai para estego-capas.csv.
 *
 * Ocupacoes (fracao da capacidade): 0% (capa limpa, linha de controle do
 * detector), 1%, 10%, 50% e 100%, fixadas antes de medir.
 *
 * Payload: ciphertext real da CifraSigea (nonce || corpo), do maior tamanho
 * que cabe na ocupacao pedida; o tamanho efetivo vai para o CSV. Cada
 * embutimento e conferido de volta: extrair e decifrar tem de devolver o
 * texto claro. Este teste mora no pacote cripto, e nao em cripto.estego,
 * porque usa cifrarComNonce com nonce de semente fixa - o CSV tem de sair
 * identico a cada execucao, e o metodo e de pacote de proposito.
 *
 * Duas leituras do detector, porque o embutimento e sequencial (mais o
 * controle: o mesmo trecho medido na capa limpa):
 *   - p na imagem inteira, como um analista sem pista faria;
 *   - p so no trecho que recebeu o payload (o comeco da imagem), e a curva
 *     acumulada em estego-curva.csv, como Westfeld mostra: o valor-p fica
 *     alto enquanto o trecho analisado esta dentro do payload e despenca
 *     depois. Nenhuma assercao sobre o detector acertar - e medida.
 */
class ExperimentoEsteganografiaTest {

    private static final long SEMENTE = 0x57E6L;
    private static final int[] OCUPACOES_PCT = {0, 1, 10, 50, 100};
    private static final int PONTOS_CURVA = 20;
    private static final String AVISO = "Aviso da turma de Ciberseguranca: a aula de quinta foi cancelada. "
            + "A prova pratica fica remarcada para sexta, 14h, no laboratorio 302. ";

    @Test
    @DisplayName("PSNR, capacidade usada e qui-quadrado LSB em todos os PNG versionados")
    void medirEsteganografia() throws IOException {
        Path raiz = RepositorioGit.raiz();
        List<String> pngs = RepositorioGit.arquivosVersionados(raiz).stream()
                .filter(r -> r.toLowerCase().endsWith(".png"))
                .collect(Collectors.toList());
        assertFalse(pngs.isEmpty(), "nenhum PNG versionado");

        Evidencias.Csv capas = new Evidencias.Csv("estego-capas.csv", "arquivo,bytes_arquivo,largura,altura,sha256,uso");
        Map<String, byte[]> unicas = new LinkedHashMap<>();
        Map<String, String> primeiraPorHash = new LinkedHashMap<>();
        for (String relativo : pngs) {
            assertFalse(relativo.contains(","), "virgula no nome quebraria o CSV: " + relativo);
            byte[] arquivo = Files.readAllBytes(raiz.resolve(relativo));
            String hash = sha256(arquivo);
            int[] dim = dimensoes(arquivo);
            String uso = primeiraPorHash.containsKey(hash) ? "duplicata_de_" + primeiraPorHash.get(hash) : "capa";
            primeiraPorHash.putIfAbsent(hash, relativo);
            if (uso.equals("capa")) {
                unicas.put(relativo, arquivo);
            }
            capas.linha("%s,%d,%d,%d,%s,%s", relativo, arquivo.length, dim[0], dim[1], hash, uso);
        }
        capas.gravar();

        Evidencias.Csv estego = new Evidencias.Csv("estego.csv",
                "imagem,largura,altura,capacidade_bytes,ocupacao_alvo_pct,payload_bytes,capacidade_usada_pct,"
                        + "valores_de_canal,valores_alterados,psnr_db,qui2_p_imagem_inteira,qui2_graus_imagem_inteira,"
                        + "valores_no_trecho_embutido,qui2_p_trecho_embutido,qui2_graus_trecho_embutido,"
                        + "qui2_p_mesmo_trecho_capa_limpa,qui2_graus_mesmo_trecho_capa_limpa");
        Evidencias.Csv curva = new Evidencias.Csv("estego-curva.csv",
                "imagem,ocupacao_alvo_pct,trecho_analisado_pct,valores_analisados,qui2_p,qui2_graus");

        Random rnd = new Random(SEMENTE);
        byte[] chave = new byte[CifraSigea.TAMANHO_CHAVE];
        rnd.nextBytes(chave);
        CifraSigea cifra = new CifraSigea(chave);

        for (Map.Entry<String, byte[]> capa : unicas.entrySet()) {
            String nome = capa.getKey();
            byte[] png = capa.getValue();
            int[] original = EsteganografiaLSB.pixels(png);
            int[] dim = dimensoes(png);
            int capacidade = EsteganografiaLSB.capacidade(dim[0], dim[1]);
            long valores = (long) original.length * 3;

            for (int ocupacao : OCUPACOES_PCT) {
                byte[] payload = new byte[0];
                int[] alterada = original;
                if (ocupacao > 0) {
                    payload = payloadCifrado(cifra, rnd, (long) capacidade * ocupacao / 100);
                    byte[] estegoPng = EsteganografiaLSB.embutir(png, payload);
                    alterada = EsteganografiaLSB.pixels(estegoPng);
                    conferirIdaEVolta(cifra, payload, EsteganografiaLSB.extrair(estegoPng), nome, ocupacao);
                }

                ResultadoQuiQuadrado inteira = AnaliseEsteganografia.quiQuadrado(alterada);
                String trecho = ",,,,";
                long valoresNoTrecho = 0;
                if (ocupacao > 0) {
                    valoresNoTrecho = (long) (EsteganografiaLSB.TAMANHO_CABECALHO + payload.length) * 8;
                    ResultadoQuiQuadrado t = AnaliseEsteganografia.quiQuadrado(alterada, valoresNoTrecho);
                    // Controle: o mesmo trecho da capa sem payload. Sem ele, um
                    // p alto no trecho poderia ser so a cara daquela regiao da
                    // tela (uma faixa de cor chapada, por exemplo).
                    ResultadoQuiQuadrado limpo = AnaliseEsteganografia.quiQuadrado(original, valoresNoTrecho);
                    trecho = Evidencias.fmt("%d,%s,%d,%s,%d", valoresNoTrecho, p(t), t.grausLiberdade(),
                            p(limpo), limpo.grausLiberdade());
                }
                estego.linha("%s,%d,%d,%d,%d,%d,%.4f,%d,%d,%s,%s,%d,%s", nome, dim[0], dim[1], capacidade, ocupacao,
                        payload.length, 100.0 * payload.length / capacidade, valores, alterados(original, alterada),
                        psnr(original, alterada), p(inteira), inteira.grausLiberdade(), trecho);

                ResultadoQuiQuadrado[] pontos = AnaliseEsteganografia.curvaQuiQuadrado(alterada, PONTOS_CURVA);
                for (int i = 0; i < pontos.length; i++) {
                    curva.linha("%s,%d,%d,%d,%s,%d", nome, ocupacao, 100 * (i + 1) / PONTOS_CURVA,
                            pontos[i].valoresAnalisados(), p(pontos[i]), pontos[i].grausLiberdade());
                }
            }
        }
        estego.gravar();
        curva.gravar();
        assertEquals(unicas.size() * OCUPACOES_PCT.length, estego.linhasDeDados());
    }

    /**
     * Ciphertext do maior tamanho que cabe em {@code alvo} bytes. A
     * CifraSigea sempre devolve nonce (12) + blocos de 16 com PKCS#7, entao
     * n blocos cabem se 12 + 16n <= alvo, e um texto claro de 16(n-1) bytes
     * produz exatamente n blocos (o ultimo e so padding).
     */
    private static byte[] payloadCifrado(CifraSigea cifra, Random rnd, long alvo) {
        long blocos = (alvo - CifraSigea.TAMANHO_NONCE) / CifraSigea.TAMANHO_BLOCO;
        assertTrue(blocos >= 1, "ocupacao pequena demais para um bloco: " + alvo + " bytes");
        byte[] claro = textoClaro((int) (CifraSigea.TAMANHO_BLOCO * (blocos - 1)));
        byte[] nonce = new byte[CifraSigea.TAMANHO_NONCE];
        rnd.nextBytes(nonce);
        byte[] corpo = cifra.cifrarComNonce(claro, nonce).textoCifrado();
        byte[] payload = Arrays.copyOf(nonce, nonce.length + corpo.length);
        System.arraycopy(corpo, 0, payload, nonce.length, corpo.length);
        assertTrue(payload.length <= alvo);
        return payload;
    }

    private static byte[] textoClaro(int tamanho) {
        byte[] aviso = AVISO.getBytes(StandardCharsets.UTF_8);
        byte[] texto = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            texto[i] = aviso[i % aviso.length];
        }
        return texto;
    }

    private static void conferirIdaEVolta(CifraSigea cifra, byte[] payload, byte[] extraido, String nome, int ocupacao) {
        assertArrayEquals(payload, extraido, nome + " a " + ocupacao + "%");
        byte[] nonce = Arrays.copyOfRange(extraido, 0, CifraSigea.TAMANHO_NONCE);
        byte[] corpo = Arrays.copyOfRange(extraido, CifraSigea.TAMANHO_NONCE, extraido.length);
        assertArrayEquals(textoClaro(corpo.length - CifraSigea.TAMANHO_BLOCO), cifra.decifrar(nonce, corpo), nome + " a " + ocupacao + "%");
    }

    private static long alterados(int[] a, int[] b) {
        long n = 0;
        for (int i = 0; i < a.length; i++) {
            int x = a[i] ^ b[i];
            n += ((x >>> 16) & 1) + ((x >>> 8) & 1) + (x & 1);
        }
        return n;
    }

    private static String psnr(int[] a, int[] b) {
        double v = AnaliseEsteganografia.psnr(a, b);
        return Double.isInfinite(v) ? "inf" : Evidencias.fmt("%.4f", v);
    }

    private static String p(ResultadoQuiQuadrado r) {
        return Double.isNaN(r.pValor()) ? "nan" : Evidencias.fmt("%.6e", r.pValor());
    }

    /** Largura e altura do chunk IHDR, que o PNG exige logo apos a assinatura (bytes 16 a 23). */
    private static int[] dimensoes(byte[] png) {
        assertEquals("IHDR", new String(png, 12, 4, StandardCharsets.US_ASCII), "PNG sem IHDR na posicao padrao");
        java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(png, 16, 8);
        return new int[]{b.getInt(), b.getInt()};
    }

    private static String sha256(byte[] dados) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
