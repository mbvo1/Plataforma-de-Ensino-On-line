package dev.com.sigea.dominio.comunicacao.aviso;

import java.io.Serializable;
import java.util.Objects;

public record AvisoId(String valor) implements Serializable {
    public AvisoId {
        Objects.requireNonNull(valor);
    }
}