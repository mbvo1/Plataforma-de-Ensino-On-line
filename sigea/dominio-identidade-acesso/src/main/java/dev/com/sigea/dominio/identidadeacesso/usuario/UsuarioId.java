package dev.com.sigea.dominio.identidadeacesso.usuario;

import java.io.Serializable;

public record UsuarioId(Long value) implements Serializable {
    public UsuarioId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo ou negativo.");
        }
    }
}