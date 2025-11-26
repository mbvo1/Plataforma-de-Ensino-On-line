package dev.com.sigea.dominio.turma;

import java.io.Serializable;
import java.util.Objects;

public record EnvioId(String valor) implements Serializable {
    public EnvioId {
        Objects.requireNonNull(valor);
    }
}