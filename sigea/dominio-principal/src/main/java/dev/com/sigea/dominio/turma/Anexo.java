package dev.com.sigea.dominio.turma;

import java.io.Serializable;
import java.util.Objects;

public record Anexo(String nomeArquivo) implements Serializable {
    public Anexo {
        Objects.requireNonNull(nomeArquivo);
    }
}