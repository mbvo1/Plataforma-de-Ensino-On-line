package dev.com.sigea.dominio.turma;

import java.io.Serializable;
import java.util.Objects;

public record MaterialId(String valor) implements Serializable {
    public MaterialId {
        Objects.requireNonNull(valor);
    }
}