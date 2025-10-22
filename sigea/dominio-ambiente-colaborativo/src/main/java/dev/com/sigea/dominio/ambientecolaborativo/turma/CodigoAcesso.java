package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.io.Serializable;
import java.util.Objects;

public record CodigoAcesso(String valor) implements Serializable {
    public CodigoAcesso {
        Objects.requireNonNull(valor);
    }
}