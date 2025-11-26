package dev.com.sigea.dominio.turma;

import java.io.Serializable;
import java.util.Objects;

public record TurmaId(String valor) implements Serializable {
    public TurmaId {
        Objects.requireNonNull(valor);
    }
}