package dev.com.sigea.dominio.gestaoacademica.forum;

import java.io.Serializable;
import java.util.Objects;

public record TopicoId(String valor) implements Serializable {
    public TopicoId {
        Objects.requireNonNull(valor);
    }
}