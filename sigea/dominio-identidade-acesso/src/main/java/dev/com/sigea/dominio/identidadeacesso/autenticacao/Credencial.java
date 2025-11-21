package dev.com.sigea.dominio.identidadeacesso.autenticacao;

import java.util.Objects;

public class Credencial {
    
    private final String email;
    private final String senha;
    
    public Credencial(String email, String senha) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        this.email = email.trim().toLowerCase();
        this.senha = senha;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getSenha() {
        return senha;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credencial that = (Credencial) o;
        return Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

