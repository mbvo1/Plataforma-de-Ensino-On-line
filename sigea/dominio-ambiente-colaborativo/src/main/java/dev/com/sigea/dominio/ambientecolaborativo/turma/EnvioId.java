package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.io.Serializable;
import java.util.Objects;

public record EnvioId(String valor) implements Serializable {
    public EnvioId {
        Objects.requireNonNull(valor);
    }
}
