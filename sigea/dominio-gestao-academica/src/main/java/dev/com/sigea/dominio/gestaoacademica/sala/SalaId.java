package dev.com.sigea.dominio.gestaoacademica.sala;

import java.io.Serializable;
import java.util.Objects;

public record SalaId(String valor) implements Serializable {
    public SalaId {
        Objects.requireNonNull(valor);
    }
}