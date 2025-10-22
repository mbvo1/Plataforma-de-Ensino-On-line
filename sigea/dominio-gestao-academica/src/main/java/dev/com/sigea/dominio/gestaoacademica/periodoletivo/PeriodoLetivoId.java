package dev.com.sigea.dominio.gestaoacademica.periodoletivo;

import java.io.Serializable;
import java.util.Objects;

public record PeriodoLetivoId(String valor) implements Serializable {
    public PeriodoLetivoId {
        Objects.requireNonNull(valor);
    }
}