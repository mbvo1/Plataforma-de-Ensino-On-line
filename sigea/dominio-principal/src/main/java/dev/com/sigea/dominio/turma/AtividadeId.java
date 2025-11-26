package dev.com.sigea.dominio.turma;

import java.io.Serializable;
import java.util.Objects;

public record AtividadeId(String valor) implements Serializable {
    public AtividadeId {
        Objects.requireNonNull(valor);
    }
}