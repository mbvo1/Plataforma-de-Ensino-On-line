package dev.com.sigea.dominio.cripto.estego;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Esteganografia LSB de 1 bit em PNG: esconde um payload nos bits menos
 * significativos dos canais R, G e B.
 *
 * Esconder nao e cifrar. O LSB so disfarca a EXISTENCIA da mensagem; quem
 * sabe onde olhar le os bits direto. Por isso o payload esperado aqui e um
 * ciphertext (CifraSigea): a imagem esconde que ha mensagem, a cifra
 * protege o conteudo. E a ordem importa - cifrar antes de embutir - porque
 * texto claro tem estatistica de lingua, e ciphertext parece ruido.
 *
 * Por que so PNG: PNG e compressao SEM perda, o pixel gravado e o pixel
 * lido. JPEG quantiza coeficientes da DCT e reescreve justamente os bits
 * baixos, entao o payload nao sobrevive a uma regravacao; GIF tem paleta de
 * 256 cores e o LSB do pixel e um indice de paleta, nao um nivel de cor.
 * A validacao e pela assinatura do arquivo (os 8 primeiros bytes), nunca
 * pela extensao do nome, que o cliente escolhe. O UploadController aceita
 * jpg e gif para anexos; quem expuser esta funcionalidade numa tela precisa
 * de allowlist propria, e esta classe ja recusa sozinha o que nao e PNG.
 *
 * Formato embutido, nesta ordem, um bit por canal:
 *   - cabecalho: tamanho do payload, 4 bytes big-endian;
 *   - payload: os bytes, cada um do bit mais para o menos significativo.
 * Percurso: linha a linha (y), pixel a pixel (x), canais R, G, B. O canal
 * alfa nao e tocado: mexer nele altera a transparencia, e pixel totalmente
 * transparente com alfa 1 vira artefato visivel sobre fundo escuro.
 *
 * Capacidade: largura x altura x 3 / 8 bytes, menos os 4 do cabecalho.
 *
 * FRAQUEZAS CONHECIDAS - documentadas para a Parte 4:
 *   - Embutimento sequencial, sem chave de percurso. O payload ocupa o
 *     comeco da imagem, e o ataque qui-quadrado de Westfeld e Pfitzmann
 *     (AnaliseEsteganografia) aplicado aos primeiros pixels o encontra
 *     mesmo quando a imagem inteira parece limpa. Embutir em posicoes
 *     sorteadas por uma chave dificultaria isso; nao foi feito.
 *   - LSB substitui o bit em vez de somar +-1 (LSB matching), e e isso que
 *     iguala as contagens de cada par de valores (2k, 2k+1) e deixa a
 *     assinatura que o qui-quadrado detecta.
 *   - Sem autenticacao no cabecalho: uma imagem sem payload ainda produz um
 *     "tamanho" a partir dos bits que ja estavam la. Se couber na
 *     capacidade, extrair devolve lixo sem erro. Quem detecta isso e a camada
 *     de cima (a decifragem tende a falhar no padding), e a CifraSigea tambem
 *     nao autentica - ver as fraquezas dela.
 */
public final class EsteganografiaLSB {

    public static final int TAMANHO_CABECALHO = 4;

    private static final byte[] ASSINATURA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] ASSINATURA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final int CANAIS = 3;

    /**
     * Teto de pixels antes de decodificar. Um PNG de poucos KB pode declarar
     * 50.000 x 50.000 pixels e pedir 10 GB de memoria na decodificacao
     * (bomba de descompressao); o cabecalho e lido antes, e a imagem e
     * recusada sem alocar nada. 40 megapixels cobre qualquer foto de celular.
     */
    static final long MAX_PIXELS = 40_000_000L;

    private EsteganografiaLSB() {
    }

    /** Bytes de payload que cabem numa imagem desse tamanho, ja descontado o cabecalho. */
    public static int capacidade(int largura, int altura) {
        long bits = (long) largura * altura * CANAIS;
        return (int) Math.max(0, bits / 8 - TAMANHO_CABECALHO);
    }

    public static int capacidade(byte[] png) {
        Imagem imagem = ler(png);
        return capacidade(imagem.largura, imagem.altura);
    }

    /** Devolve um PNG novo com o payload embutido; o original nao e alterado. */
    public static byte[] embutir(byte[] png, byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload nao pode ser nulo (use array vazio)");
        }
        Imagem imagem = ler(png);
        int capacidade = capacidade(imagem.largura, imagem.altura);
        if (payload.length > capacidade) {
            throw new FalhaEsteganografia("Payload de " + payload.length + " bytes nao cabe na imagem "
                    + imagem.largura + "x" + imagem.altura + ": capacidade de " + capacidade
                    + " bytes (largura x altura x 3 / 8, menos " + TAMANHO_CABECALHO + " de cabecalho)");
        }

        byte[] fluxo = new byte[TAMANHO_CABECALHO + payload.length];
        fluxo[0] = (byte) (payload.length >>> 24);
        fluxo[1] = (byte) (payload.length >>> 16);
        fluxo[2] = (byte) (payload.length >>> 8);
        fluxo[3] = (byte) payload.length;
        System.arraycopy(payload, 0, fluxo, TAMANHO_CABECALHO, payload.length);

        int[] argb = imagem.argb.clone();
        long totalBits = (long) fluxo.length * 8;
        for (int i = 0; i < totalBits; i++) {
            int bit = (fluxo[i >>> 3] >>> (7 - (i & 7))) & 1;
            int pixel = i / CANAIS;
            int deslocamento = deslocamentoDoCanal(i % CANAIS);
            argb[pixel] = (argb[pixel] & ~(1 << deslocamento)) | (bit << deslocamento);
        }
        return gravar(imagem, argb);
    }

    public static byte[] extrair(byte[] png) {
        Imagem imagem = ler(png);
        int capacidade = capacidade(imagem.largura, imagem.altura);
        if (capacidade == 0 && imagem.largura * (long) imagem.altura * CANAIS < TAMANHO_CABECALHO * 8L) {
            throw new FalhaEsteganografia("Imagem pequena demais para conter o cabecalho");
        }
        int tamanho = (int) lerBits(imagem.argb, 0, TAMANHO_CABECALHO);
        if (tamanho < 0 || tamanho > capacidade) {
            throw new FalhaEsteganografia("Nenhum payload LSB valido: o cabecalho indica " + (tamanho & 0xFFFFFFFFL)
                    + " bytes e a capacidade e " + capacidade);
        }
        byte[] payload = new byte[tamanho];
        for (int b = 0; b < tamanho; b++) {
            payload[b] = (byte) lerBits(imagem.argb, (long) (TAMANHO_CABECALHO + b) * 8, 1);
        }
        return payload;
    }

    /**
     * Os pixels de um PNG, ja validado, como ARGB de 8 bits por canal, na
     * ordem de percurso do embutimento. Para as metricas de
     * AnaliseEsteganografia lerem exatamente o que esta classe le.
     */
    public static int[] pixels(byte[] png) {
        return ler(png).argb.clone();
    }

    private static long lerBits(int[] argb, long bitInicial, int bytes) {
        long valor = 0;
        for (long i = bitInicial; i < bitInicial + bytes * 8L; i++) {
            int pixel = (int) (i / CANAIS);
            valor = (valor << 1) | ((argb[pixel] >>> deslocamentoDoCanal((int) (i % CANAIS))) & 1);
        }
        return valor;
    }

    /** R, G, B dentro do int ARGB: bits 16, 8 e 0. O alfa (bits 24-31) nunca aparece aqui. */
    static int deslocamentoDoCanal(int canal) {
        return 16 - 8 * canal;
    }

    // ------------------------------------------------------------------
    // Leitura e gravacao do PNG
    // ------------------------------------------------------------------

    private static final class Imagem {
        final int largura;
        final int altura;
        final boolean temAlfa;
        final int[] argb;

        Imagem(int largura, int altura, boolean temAlfa, int[] argb) {
            this.largura = largura;
            this.altura = altura;
            this.temAlfa = temAlfa;
            this.argb = argb;
        }
    }

    private static Imagem ler(byte[] arquivo) {
        validarAssinatura(arquivo);
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(arquivo))) {
            Iterator<ImageReader> leitores = ImageIO.getImageReadersByFormatName("png");
            if (!leitores.hasNext()) {
                throw new IllegalStateException("JVM sem leitor de PNG");
            }
            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(entrada, true, true);
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if ((long) largura * altura > MAX_PIXELS) {
                    throw new FalhaEsteganografia("Imagem de " + largura + "x" + altura
                            + " excede o limite de " + MAX_PIXELS + " pixels");
                }
                BufferedImage imagem = leitor.read(0);
                ColorModel modelo = imagem.getColorModel();
                // 16 bits por canal: getRGB reduziria a 8 bits e a imagem de
                // saida perderia precisao em todos os pixels, nao so no LSB.
                // Melhor recusar do que degradar a capa sem avisar.
                for (int tamanho : modelo.getComponentSize()) {
                    if (tamanho > 8) {
                        throw new FalhaEsteganografia("PNG com " + tamanho + " bits por canal nao suportado; use 8 bits");
                    }
                }
                int[] argb = imagem.getRGB(0, 0, largura, altura, null, 0, largura);
                return new Imagem(largura, altura, modelo.hasAlpha(), argb);
            } finally {
                leitor.dispose();
            }
        } catch (IOException e) {
            throw new FalhaEsteganografia("PNG corrompido ou ilegivel: " + e.getMessage(), e);
        }
    }

    private static void validarAssinatura(byte[] arquivo) {
        if (arquivo == null || arquivo.length == 0) {
            throw new FalhaEsteganografia("Arquivo vazio");
        }
        if (comecaCom(arquivo, ASSINATURA_PNG)) {
            return;
        }
        if (comecaCom(arquivo, ASSINATURA_JPEG)) {
            throw new FalhaEsteganografia("JPEG recusado: a compressao com perda reescreve os bits menos "
                    + "significativos e destroi o payload. Use PNG.");
        }
        throw new FalhaEsteganografia("Formato recusado: apenas PNG e aceito (assinatura do arquivo nao confere)");
    }

    private static boolean comecaCom(byte[] dados, byte[] prefixo) {
        return dados.length >= prefixo.length && Arrays.equals(dados, 0, prefixo.length, prefixo, 0, prefixo.length);
    }

    /**
     * Sempre PNG truecolor de 8 bits (RGB, ou ARGB se a original tinha
     * transparencia). Uma capa em paleta ou em tons de cinza nao consegue
     * representar o pixel com o LSB alterado, entao a saida muda de tipo;
     * os niveis de cor de cada pixel continuam os mesmos fora do LSB.
     */
    private static byte[] gravar(Imagem original, int[] argb) {
        BufferedImage saida = new BufferedImage(original.largura, original.altura,
                original.temAlfa ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        saida.setRGB(0, 0, original.largura, original.altura, argb, 0, original.largura);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(saida, "png", bytes)) {
                throw new IllegalStateException("JVM sem gravador de PNG");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar PNG em memoria", e);
        }
        return bytes.toByteArray();
    }

    /** Recusa com motivo legivel: formato, capacidade, cabecalho invalido ou arquivo corrompido. */
    public static final class FalhaEsteganografia extends RuntimeException {
        public FalhaEsteganografia(String mensagem) {
            super(mensagem);
        }

        public FalhaEsteganografia(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }
}
