package dev.com.sigea.dominio.usuario;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.io.Serializable;
import java.util.Objects;

public class Senha implements Serializable {

    // Mitigação V-01: Argon2id via Spring Security, com salt aleatório
    // por senha e fator de custo deliberadamente alto (nao e hash rapido,
    // e KDF lenta de proposito).
    private static final Argon2PasswordEncoder ENCODER =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    private final String senhaHash;

    public Senha(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        this.senhaHash = senhaHash;
    }

    /** Cria uma nova Senha a partir do texto puro, aplicando o hash Argon2id. */
    public static Senha criarNova(String senhaTexto) {
        return new Senha(ENCODER.encode(senhaTexto));
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    /** Verifica a senha em texto puro contra o hash armazenado. */
    public boolean verificar(String senhaTextoPura) {
        return ENCODER.matches(senhaTextoPura, this.senhaHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Senha senha = (Senha) o;
        return Objects.equals(senhaHash, senha.senhaHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senhaHash);
    }
}