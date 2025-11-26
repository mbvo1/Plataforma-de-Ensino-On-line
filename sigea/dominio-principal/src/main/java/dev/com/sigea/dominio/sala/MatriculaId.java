package dev.com.sigea.dominio.sala;

import java.io.Serializable;
import java.util.Objects;

public record MatriculaId(String valor) implements Serializable {
    public MatriculaId {
        Objects.requireNonNull(valor);
    }
}