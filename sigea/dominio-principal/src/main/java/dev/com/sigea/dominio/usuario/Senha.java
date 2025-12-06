package dev.com.sigea.dominio.usuario;

import java.io.Serializable;
import java.util.Objects;

public class Senha implements Serializable {
    
    private final String senhaHash;
    
    public Senha(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        this.senhaHash = senhaHash;
    }
    
    public String getSenhaHash() {
        return senhaHash;
    }
    
    public boolean verificar(String senhaHash) {
        return this.senhaHash.equals(senhaHash);
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
