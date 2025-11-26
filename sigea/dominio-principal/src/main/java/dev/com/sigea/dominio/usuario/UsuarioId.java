package dev.com.sigea.dominio.usuario;

import java.io.Serializable;
import java.util.Objects;

public record UsuarioId(String valor) implements Serializable {
    public UsuarioId {
        Objects.requireNonNull(valor, "O valor do ID do usuário não pode ser nulo.");
    }
}