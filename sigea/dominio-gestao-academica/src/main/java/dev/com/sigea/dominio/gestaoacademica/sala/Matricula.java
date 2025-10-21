package dev.com.sigea.dominio.gestaoacademica.sala;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

import java.util.Objects;

public class Matricula {
    private final MatriculaId id;
    private final UsuarioId alunoId;
    private MatriculaStatus status;

    Matricula(MatriculaId id, UsuarioId alunoId) {
        this.id = Objects.requireNonNull(id);
        this.alunoId = Objects.requireNonNull(alunoId);
        this.status = MatriculaStatus.ATIVA;
    }

    public UsuarioId getAlunoId() {
        return alunoId;
    }

    public MatriculaStatus getStatus() {
        return status;
    }
}