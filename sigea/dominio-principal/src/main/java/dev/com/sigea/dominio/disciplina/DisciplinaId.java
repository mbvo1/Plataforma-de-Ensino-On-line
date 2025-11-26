package dev.com.sigea.dominio.disciplina;

import java.io.Serializable;
import java.util.Objects;

public record DisciplinaId(String valor) implements Serializable {
    public DisciplinaId {
        Objects.requireNonNull(valor, "O valor do ID da disciplina não pode ser nulo.");
    }
}