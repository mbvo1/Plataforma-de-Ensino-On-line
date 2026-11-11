package dev.com.sigea.dominio.cripto;

import java.util.Base64;

/**
 * Saida de uma cifragem: nonce, texto cifrado e tempo gasto.
 *
 * Por que o tempo viaja junto do resultado: a Fase 2 compara o custo da
 * cifra propria com o do AES-256-GCM, e medir por fora (em volta da
 * chamada) misturaria geracao de nonce, alocacao e Base64 no numero.
 * Aqui o cronometro cobre escalonamento + padding + CBC, que e o trabalho
 * criptografico de fato.
 *
 * Classe e nao record de proposito: record com campo byte[] compara por
 * referencia no equals e expoe o array mutavel no acessor. As copias
 * defensivas garantem que ninguem altere o nonce depois de cifrar - um
 * nonce alterado ainda decifra, so que para lixo, e o bug seria silencioso.
 */
public final class ResultadoCifragem {

    /** Mesmo prefixo de versao do CpfCryptoConverter: permite trocar o esquema no futuro sem ambiguidade. */
    public static final String PREFIXO_VERSAO = "v1:";

    private final byte[] nonce;
    private final byte[] textoCifrado;
    private final long tempoNanos;

    public ResultadoCifragem(byte[] nonce, byte[] textoCifrado, long tempoNanos) {
        this.nonce = nonce.clone();
        this.textoCifrado = textoCifrado.clone();
        this.tempoNanos = tempoNanos;
    }

    public byte[] nonce() {
        return nonce.clone();
    }

    public byte[] textoCifrado() {
        return textoCifrado.clone();
    }

    public long tempoNanos() {
        return tempoNanos;
    }

    /** {@code v1:} + Base64(nonce || ciphertext) - formato que vai para a coluna TEXT. */
    public String paraTextoVersionado() {
        byte[] payload = new byte[nonce.length + textoCifrado.length];
        System.arraycopy(nonce, 0, payload, 0, nonce.length);
        System.arraycopy(textoCifrado, 0, payload, nonce.length, textoCifrado.length);
        return PREFIXO_VERSAO + Base64.getEncoder().encodeToString(payload);
    }

    @Override
    public String toString() {
        return "ResultadoCifragem[" + textoCifrado.length + " bytes cifrados, " + tempoNanos + " ns]";
    }
}
