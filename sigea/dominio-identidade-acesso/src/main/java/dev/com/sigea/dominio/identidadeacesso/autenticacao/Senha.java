package dev.com.sigea.dominio.identidadeacesso.autenticacao;

import java.util.Objects;

public class Senha {
    
    private final String hash;
    
    protected Senha(String hash) {
        this.hash = Objects.requireNonNull(hash, "Hash da senha não pode ser nulo.");
    }
    
    public static Senha criar(String senhaPlana) {
        if (senhaPlana == null || senhaPlana.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }
        if (senhaPlana.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres.");
        }
        String hash = String.valueOf(senhaPlana.hashCode());
        return new Senha(hash);
    }
    
    public static Senha deHash(String hash) {
        return new Senha(hash);
    }
    
    public String getHash() {
        return hash;
    }
    
    public boolean verificar(String senhaPlana) {
        String hashSenhaPlana = String.valueOf(senhaPlana.hashCode());
        return hash.equals(hashSenhaPlana);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Senha senha = (Senha) o;
        return Objects.equals(hash, senha.hash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}

