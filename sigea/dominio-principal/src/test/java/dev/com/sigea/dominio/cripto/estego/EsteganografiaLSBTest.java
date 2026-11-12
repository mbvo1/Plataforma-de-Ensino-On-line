package dev.com.sigea.dominio.cripto.estego;

import dev.com.sigea.dominio.cripto.CifraSigea;
import dev.com.sigea.dominio.cripto.ResultadoCifragem;
import dev.com.sigea.dominio.cripto.estego.EsteganografiaLSB.FalhaEsteganografia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Criterios de aceite da Fase 3 e o contrato do formato. As medicoes sobre
 * as capas reais (estego.csv) ficam em ExperimentoEsteganografiaTest.
 */
class EsteganografiaLSBTest {

    private static final long SEMENTE = 0x57E60L;
    private static final String AVISO = "Aviso da turma de Ciberseguranca: a aula de quinta foi cancelada. "
            + "A prova pratica fica remarcada para sexta, 14h, no laboratorio 302. Tragam o notebook carregado.";

    /** Capa sintetica com cara de foto: gradiente suave mais ruido de sensor, semente fixa. */
    static BufferedImage capaSintetica(int largura, int altura, int tipo, long semente) {
        Random rnd = new Random(semente);
        BufferedImage img = new BufferedImage(largura, altura, tipo);
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int r = limitar(40 + 180 * x / largura + (int) (rnd.nextGaussian() * 6));
                int g = limitar(60 + 120 * y / altura + (int) (rnd.nextGaussian() * 6));
                int b = limitar(200 - 150 * (x + y) / (largura + altura) + (int) (rnd.nextGaussian() * 6));
                int a = tipo == BufferedImage.TYPE_INT_ARGB ? rnd.nextInt(256) : 255;
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    private static int limitar(int v) {
        return Math.max(0, Math.min(255, v));
    }

    static byte[] codificar(BufferedImage img, String formato) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            assertTrue(ImageIO.write(img, formato, saida), "JVM sem gravador de " + formato);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return saida.toByteArray();
    }

    private static byte[] png(int largura, int altura) {
        return codificar(capaSintetica(largura, altura, BufferedImage.TYPE_INT_RGB, SEMENTE), "png");
    }

    private static byte[] concatenar(byte[] a, byte[] b) {
        byte[] c = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static void idaEVoltaComAviso(byte[] capa) {
        byte[] chave = new byte[CifraSigea.TAMANHO_CHAVE];
        new Random(SEMENTE).nextBytes(chave);
        CifraSigea cifra = new CifraSigea(chave);
        ResultadoCifragem cifrado = cifra.cifrar(AVISO.getBytes(StandardCharsets.UTF_8));
        // nonce || ciphertext em binario: o mesmo conteudo do formato v1 da
        // coluna, sem o Base64, que gastaria 33% a mais de capacidade.
        byte[] payload = concatenar(cifrado.nonce(), cifrado.textoCifrado());

        byte[] estego = EsteganografiaLSB.embutir(capa, payload);
        byte[] extraido = EsteganografiaLSB.extrair(estego);

        assertArrayEquals(payload, extraido);
        byte[] nonce = Arrays.copyOfRange(extraido, 0, CifraSigea.TAMANHO_NONCE);
        byte[] corpo = Arrays.copyOfRange(extraido, CifraSigea.TAMANHO_NONCE, extraido.length);
        assertEquals(AVISO, new String(cifra.decifrar(nonce, corpo), StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------
    // Criterios de aceite
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Aceite 1: embutir o ciphertext de um aviso, extrair, decifrar e obter o texto original")
    void idaEVoltaAvisoCifrado() {
        idaEVoltaComAviso(png(320, 200));
    }

    @Test
    @DisplayName("Aceite 1 numa captura de tela real do repositorio")
    void idaEVoltaAvisoCifradoEmPngReal() throws IOException {
        Path real = Paths.get("../docs/evidencias/v05/05-app-rodando.png");
        assertTrue(Files.isRegularFile(real), "capa real ausente: " + real.toAbsolutePath());
        idaEVoltaComAviso(Files.readAllBytes(real));
    }

    @Test
    @DisplayName("Aceite 2: PSNR acima de 40 dB mesmo com a capacidade inteira ocupada")
    void psnrAcimaDe40dB() {
        byte[] capa = png(320, 200);
        byte[] payload = new byte[EsteganografiaLSB.capacidade(capa)];
        new Random(SEMENTE).nextBytes(payload);
        byte[] estego = EsteganografiaLSB.embutir(capa, payload);

        int[] antes = EsteganografiaLSB.pixels(capa);
        int[] depois = EsteganografiaLSB.pixels(estego);
        double psnr = AnaliseEsteganografia.psnr(antes, depois);
        // Limite teorico: cada canal muda no maximo 1 nivel, MSE <= 1, PSNR >= 48,13 dB.
        assertTrue(psnr > 40, "PSNR " + psnr);
        assertTrue(psnr >= 10 * Math.log10(255.0 * 255.0) - 1e-9, "PSNR abaixo do limite teorico: " + psnr);
    }

    @Test
    @DisplayName("Aceite 3: JPG e recusado com erro explicito, em todas as operacoes")
    void jpgRecusado() {
        byte[] jpg = codificar(capaSintetica(64, 64, BufferedImage.TYPE_INT_RGB, SEMENTE), "jpg");
        assertEquals((byte) 0xFF, jpg[0], "o JPG gerado deveria comecar com FF D8 FF");
        FalhaEsteganografia erro = assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(jpg, new byte[1]));
        assertTrue(erro.getMessage().contains("JPEG"), erro.getMessage());
        assertTrue(erro.getMessage().contains("PNG"), erro.getMessage());
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.extrair(jpg));
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.capacidade(jpg));
    }

    // ------------------------------------------------------------------
    // Formato e validacao
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GIF, bytes aleatorios, arquivo vazio e PNG truncado sao recusados")
    void outrosFormatosRecusados() {
        byte[] gif = codificar(capaSintetica(32, 32, BufferedImage.TYPE_INT_RGB, SEMENTE), "gif");
        FalhaEsteganografia erro = assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(gif, new byte[1]));
        assertTrue(erro.getMessage().contains("apenas PNG"), erro.getMessage());

        byte[] lixo = new byte[500];
        new Random(SEMENTE).nextBytes(lixo);
        lixo[0] = 0x00;
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(lixo, new byte[1]));
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(new byte[0], new byte[1]));
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(null, new byte[1]));

        byte[] truncado = Arrays.copyOf(png(64, 64), 60);
        assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.embutir(truncado, new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> EsteganografiaLSB.embutir(png(8, 8), null));
    }

    @Test
    @DisplayName("Capacidade = largura x altura x 3 / 8 - 4: o limite exato cabe, um byte a mais e recusado")
    void capacidadeExata() {
        assertEquals(100 * 100 * 3 / 8 - 4, EsteganografiaLSB.capacidade(100, 100));
        assertEquals(0, EsteganografiaLSB.capacidade(3, 3));

        byte[] capa = png(100, 100);
        int capacidade = EsteganografiaLSB.capacidade(capa);
        assertEquals(3746, capacidade);

        byte[] cheio = new byte[capacidade];
        new Random(SEMENTE).nextBytes(cheio);
        assertArrayEquals(cheio, EsteganografiaLSB.extrair(EsteganografiaLSB.embutir(capa, cheio)));

        FalhaEsteganografia erro = assertThrows(FalhaEsteganografia.class,
                () -> EsteganografiaLSB.embutir(capa, new byte[capacidade + 1]));
        assertTrue(erro.getMessage().contains("capacidade de " + capacidade), erro.getMessage());
    }

    @Test
    @DisplayName("Payload vazio vai e volta; so o cabecalho e gravado")
    void payloadVazio() {
        byte[] estego = EsteganografiaLSB.embutir(png(16, 16), new byte[0]);
        assertEquals(0, EsteganografiaLSB.extrair(estego).length);
    }

    @Test
    @DisplayName("Formato: cabecalho de 4 bytes big-endian, bits do mais significativo, R-G-B por pixel")
    void formatoDosBits() {
        // Capa toda preta: cada LSB lido e exatamente o bit embutido.
        BufferedImage preta = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        int[] px = EsteganografiaLSB.pixels(EsteganografiaLSB.embutir(codificar(preta, "png"), new byte[]{(byte) 0xA5}));
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            bits.append((px[i / 3] >>> EsteganografiaLSB.deslocamentoDoCanal(i % 3)) & 1);
        }
        assertEquals("00000000" + "00000000" + "00000000" + "00000001" + "10100101", bits.toString());
        // O resto da capa nao foi tocado.
        for (int i = 40; i < 48; i++) {
            assertEquals(0, (px[i / 3] >>> EsteganografiaLSB.deslocamentoDoCanal(i % 3)) & 1, "valor de canal " + i);
        }
    }

    @Test
    @DisplayName("Alfa intacto e so o LSB de R, G e B muda")
    void alfaIntactoESoLsbMuda() {
        byte[] capa = codificar(capaSintetica(120, 80, BufferedImage.TYPE_INT_ARGB, SEMENTE), "png");
        byte[] payload = new byte[EsteganografiaLSB.capacidade(capa)];
        new Random(SEMENTE + 1).nextBytes(payload);
        int[] antes = EsteganografiaLSB.pixels(capa);
        int[] depois = EsteganografiaLSB.pixels(EsteganografiaLSB.embutir(capa, payload));
        int mascaraLsb = 0x00010101;
        for (int i = 0; i < antes.length; i++) {
            assertEquals(antes[i] >>> 24, depois[i] >>> 24, "alfa do pixel " + i);
            assertEquals(antes[i] & ~mascaraLsb, depois[i] & ~mascaraLsb, "bits altos do pixel " + i);
        }
    }

    @Test
    @DisplayName("Imagem sem payload com cabecalho impossivel e recusada na extracao")
    void cabecalhoImpossivel() {
        // Todos os LSB em 1: o cabecalho le 0xFFFFFFFF bytes, muito acima da capacidade.
        BufferedImage branca = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 20; y++) for (int x = 0; x < 20; x++) branca.setRGB(x, y, 0xFFFFFF);
        FalhaEsteganografia erro = assertThrows(FalhaEsteganografia.class, () -> EsteganografiaLSB.extrair(codificar(branca, "png")));
        assertTrue(erro.getMessage().contains("Nenhum payload"), erro.getMessage());
    }

    @Test
    @DisplayName("PNG em paleta e aceito (vira truecolor); PNG de 16 bits por canal e recusado")
    void tiposDePng() {
        BufferedImage paleta = new BufferedImage(40, 40, BufferedImage.TYPE_BYTE_INDEXED);
        paleta.getGraphics().drawImage(capaSintetica(40, 40, BufferedImage.TYPE_INT_RGB, SEMENTE), 0, 0, null);
        byte[] payload = "oi".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(payload, EsteganografiaLSB.extrair(EsteganografiaLSB.embutir(codificar(paleta, "png"), payload)));

        BufferedImage dezesseis = new BufferedImage(40, 40, BufferedImage.TYPE_USHORT_GRAY);
        FalhaEsteganografia erro = assertThrows(FalhaEsteganografia.class,
                () -> EsteganografiaLSB.embutir(codificar(dezesseis, "png"), payload));
        assertTrue(erro.getMessage().contains("16 bits"), erro.getMessage());
    }

    // ------------------------------------------------------------------
    // Metricas
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Cauda da qui-quadrado confere com valores tabelados")
    void gamaIncompleta() {
        // Quantis de 5% da qui-quadrado (tabela padrao) e o caso fechado k=2: Q = e^(-x/2).
        assertEquals(0.05, AnaliseEsteganografia.gamaRegularizadaSuperior(0.5, 3.841458820694124 / 2), 1e-9);
        assertEquals(0.05, AnaliseEsteganografia.gamaRegularizadaSuperior(5, 18.307038053275146 / 2), 1e-9);
        assertEquals(0.05, AnaliseEsteganografia.gamaRegularizadaSuperior(50, 124.34211340400407 / 2), 1e-9);
        assertEquals(Math.exp(-1), AnaliseEsteganografia.gamaRegularizadaSuperior(1, 1), 1e-12);
        assertEquals(Math.log(24), AnaliseEsteganografia.lnGama(5), 1e-12);
    }

    @Test
    @DisplayName("Detector: capa so com valores pares parece limpa; com a capacidade cheia, os pares se igualam")
    void detectorDistingueCasoConstruido() {
        // Caso construido para checar o sentido do valor-p, nao a eficacia
        // do ataque (isso e medida, vai para estego.csv): todos os valores
        // pares e um payload de bytes aleatorios.
        Random rnd = new Random(SEMENTE);
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 200; x++) {
                img.setRGB(x, y, ((rnd.nextInt(128) * 2) << 16) | ((rnd.nextInt(128) * 2) << 8) | (rnd.nextInt(128) * 2));
            }
        }
        byte[] capa = codificar(img, "png");
        byte[] payload = new byte[EsteganografiaLSB.capacidade(capa)];
        rnd.nextBytes(payload);
        double pLimpa = AnaliseEsteganografia.quiQuadrado(EsteganografiaLSB.pixels(capa)).pValor();
        double pEstego = AnaliseEsteganografia.quiQuadrado(EsteganografiaLSB.pixels(EsteganografiaLSB.embutir(capa, payload))).pValor();
        assertTrue(pLimpa < 1e-6, "capa so com pares deveria dar p ~ 0: " + pLimpa);
        assertTrue(pEstego > 0.01, "pares igualados deveriam dar p alto: " + pEstego);
    }

    @Test
    @DisplayName("A curva acumulada bate com o calculo direto em cada trecho")
    void curvaIgualAoCalculoDireto() {
        int[] px = EsteganografiaLSB.pixels(png(90, 70));
        AnaliseEsteganografia.ResultadoQuiQuadrado[] curva = AnaliseEsteganografia.curvaQuiQuadrado(px, 10);
        for (int i = 0; i < 10; i++) {
            AnaliseEsteganografia.ResultadoQuiQuadrado direto = AnaliseEsteganografia.quiQuadrado(px, curva[i].valoresAnalisados());
            assertEquals(direto, curva[i], "trecho " + (i + 1));
        }
        assertEquals((long) px.length * 3, curva[9].valoresAnalisados());
        assertEquals(Double.POSITIVE_INFINITY, AnaliseEsteganografia.psnr(px, px.clone()));
    }
}
