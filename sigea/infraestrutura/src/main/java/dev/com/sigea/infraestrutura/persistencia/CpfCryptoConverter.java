package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Mitigacao V-07: cifra o CPF em repouso com AES-256-GCM.
 *
 * O nonce nao e aleatorio: e derivado deterministicamente via
 * HMAC-SHA256(chave, cpfPlano). Isso e proposital, nao um descuido -
 * GCM com nonce aleatorio tornaria o mesmo CPF cifrado de forma diferente
 * a cada gravacao, quebrando a restricao UNIQUE da coluna e o
 * findByCpf() usado no cadastro e na edicao de perfil (Hibernate aplica
 * este converter tambem ao parametro de query, entao nada nos controllers
 * precisou mudar). O preco dessa escolha e o aceito para um campo de
 * busca exata: duas linhas com o mesmo CPF produzem o mesmo texto cifrado
 * (o que a propria unicidade do CPF ja implicava), mas o valor em si
 * continua protegido sem a chave.
 *
 * Chave de cifragem e chave de derivacao do nonce sao subchaves distintas,
 * derivadas por separacao de dominio a partir do mesmo segredo bruto -
 * nunca a mesma chave crua e usada para dois propositos criptograficos
 * diferentes.
 */
@Component
@Converter
public class CpfCryptoConverter implements AttributeConverter<String, String> {

    private static final String PREFIXO_CIFRADO = "v1:";
    private static final int TAMANHO_NONCE_BYTES = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    @Value("${cpf.encryption.key}")
    private String segredo;

    @Override
    public String convertToDatabaseColumn(String cpfPlano) {
        if (cpfPlano == null) {
            return null;
        }
        try {
            byte[] nonce = derivarNonce(cpfPlano);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chaveAes("enc"), new GCMParameterSpec(TAMANHO_TAG_BITS, nonce));
            byte[] textoCifrado = cipher.doFinal(cpfPlano.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[nonce.length + textoCifrado.length];
            System.arraycopy(nonce, 0, payload, 0, nonce.length);
            System.arraycopy(textoCifrado, 0, payload, nonce.length, textoCifrado.length);

            return PREFIXO_CIFRADO + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar CPF", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String valorColuna) {
        if (valorColuna == null) {
            return null;
        }
        if (!valorColuna.startsWith(PREFIXO_CIFRADO)) {
            // Linha gravada antes deste fix, ainda em texto claro. Continua
            // legivel (nao ha migracao em lote nesta correcao) e sera
            // recifrada na proxima vez que o registro for salvo.
            return valorColuna;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(valorColuna.substring(PREFIXO_CIFRADO.length()));
            byte[] nonce = Arrays.copyOfRange(payload, 0, TAMANHO_NONCE_BYTES);
            byte[] textoCifrado = Arrays.copyOfRange(payload, TAMANHO_NONCE_BYTES, payload.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chaveAes("enc"), new GCMParameterSpec(TAMANHO_TAG_BITS, nonce));
            byte[] textoPlano = cipher.doFinal(textoCifrado);
            return new String(textoPlano, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar CPF", e);
        }
    }

    private byte[] derivarNonce(String cpfPlano) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(derivarSubchave("nonce"), "HmacSHA256"));
        byte[] full = hmac.doFinal(cpfPlano.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(full, TAMANHO_NONCE_BYTES);
    }

    private SecretKeySpec chaveAes(String finalidade) throws Exception {
        return new SecretKeySpec(derivarSubchave(finalidade), "AES");
    }

    /** Deriva uma subchave de 256 bits a partir do segredo bruto, com separacao de dominio por finalidade. */
    private byte[] derivarSubchave(String finalidade) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(segredo.getBytes(StandardCharsets.UTF_8));
        sha256.update((byte) ':');
        sha256.update(finalidade.getBytes(StandardCharsets.UTF_8));
        return sha256.digest();
    }
}
