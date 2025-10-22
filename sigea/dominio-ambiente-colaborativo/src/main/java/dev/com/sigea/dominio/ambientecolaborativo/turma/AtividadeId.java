package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.io.Serializable;
import java.util.Objects;

public record AtividadeId(String valor) implements Serializable {
    public AtividadeId {
        Objects.requireNonNull(valor);
    }
}
