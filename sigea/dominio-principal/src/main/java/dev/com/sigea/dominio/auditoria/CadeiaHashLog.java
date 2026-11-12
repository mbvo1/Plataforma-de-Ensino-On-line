package dev.com.sigea.dominio.auditoria;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * Hash encadeado do log_seguranca (Fase 4).
 *
 * hash_atual = SHA-256(serializacao canonica de log_id, timestamp,
 * tipo_evento, usuario_id, recurso, ip, resultado, detalhe, hash_anterior).
 * O primeiro registro usa 64 zeros como hash_anterior.
 *
 * Desvio deliberado do brief, decidido pelo grupo: a formula do brief deixa
 * ip e detalhe fora do hash. Com ela, editar o ip ou o JSON de detalhe pelo
 * console H2 passaria sem deteccao, o que contradiz o proprio criterio de
 * aceite ("alterar um registro a mao e o endpoint apontar aquele log_id").
 * Aqui toda coluna da linha, exceto o proprio hash_atual, entra no hash.
 *
 * Serializacao canonica, e nao concatenacao simples: com "a" || "bc" e
 * "ab" || "c" dando o mesmo texto, um atacante poderia mover caracteres de
 * recurso para resultado sem mudar o hash. Cada campo vai como comprimento
 * (4 bytes, big-endian) seguido dos bytes UTF-8; nulo vira comprimento -1,
 * distinto da string vazia. O timestamp vai com 9 casas de fracao, sempre,
 * para que qualquer edicao, mesmo de microssegundos, mude o texto.
 *
 * O que esta cadeia NAO protege (achados para o relatorio, nao corrigidos):
 *   - SHA-256 sem chave: quem pode escrever no banco pode alterar uma linha
 *     e recalcular todos os hashes seguintes, e a cadeia volta a fechar. A
 *     defesa e ancorar o ultimo hash fora do banco (o endpoint de
 *     verificacao devolve hashFinal para isso) ou trocar por HMAC com chave
 *     fora do banco.
 *   - Truncar o fim do log (apagar as ultimas linhas) nao quebra nenhum
 *     elo; so a ancora externa mostra que a cauda sumiu.
 */
public final class CadeiaHashLog {

    /** hash_anterior do primeiro registro. */
    public static final String HASH_GENESE = "0".repeat(64);

    /** Limites das colunas em V28; o texto e cortado ANTES do hash, para o hash bater com o que o banco guarda. */
    public static final int TAMANHO_TIPO_EVENTO = 50;
    public static final int TAMANHO_RECURSO = 255;
    public static final int TAMANHO_IP = 45;
    public static final int TAMANHO_RESULTADO = 20;

    private static final DateTimeFormatter FORMATO_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS");

    private CadeiaHashLog() {
    }

    public static String calcular(Long logId, LocalDateTime timestamp, String tipoEvento, Long usuarioId,
                                  String recurso, String ip, String resultado, String detalhe,
                                  String hashAnterior) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(
                    canonico(logId, timestamp, tipoEvento, usuarioId, recurso, ip, resultado, detalhe, hashAnterior)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
        }
    }

    /** Bytes que entram no SHA-256. Exposto para o teste de ambiguidade. */
    static byte[] canonico(Long logId, LocalDateTime timestamp, String tipoEvento, Long usuarioId,
                           String recurso, String ip, String resultado, String detalhe, String hashAnterior) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream(256);
        campo(saida, logId == null ? null : logId.toString());
        campo(saida, timestamp == null ? null : FORMATO_TIMESTAMP.format(timestamp));
        campo(saida, tipoEvento);
        campo(saida, usuarioId == null ? null : usuarioId.toString());
        campo(saida, recurso);
        campo(saida, ip);
        campo(saida, resultado);
        campo(saida, detalhe);
        campo(saida, hashAnterior);
        return saida.toByteArray();
    }

    private static void campo(ByteArrayOutputStream saida, String valor) {
        if (valor == null) {
            saida.writeBytes(ByteBuffer.allocate(4).putInt(-1).array());
            return;
        }
        byte[] bytes = valor.getBytes(StandardCharsets.UTF_8);
        saida.writeBytes(ByteBuffer.allocate(4).putInt(bytes.length).array());
        saida.writeBytes(bytes);
    }

    /**
     * Corta o texto no limite da coluna (em caracteres, como o VARCHAR do H2
     * conta), sem partir um par substituto UTF-16 ao meio.
     */
    public static String ajustar(String valor, int limite) {
        if (valor == null || valor.length() <= limite) {
            return valor;
        }
        int fim = limite;
        if (Character.isHighSurrogate(valor.charAt(fim - 1))) {
            fim--;
        }
        return valor.substring(0, fim);
    }
}
