package dev.com.sigea.dominio.gestaoacademica.sala;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Matricula {
    private final MatriculaId id;
    private final UsuarioId alunoId;
    private MatriculaStatus status;

    private final Map<String, Nota> avaliacoes = new HashMap<>();

    Matricula(MatriculaId id, UsuarioId alunoId) {
        this.id = Objects.requireNonNull(id);
        this.alunoId = Objects.requireNonNull(alunoId);
        this.status = MatriculaStatus.ATIVA;
    }

    void registrarNota(String avaliacao, Nota nota) {
        this.avaliacoes.put(avaliacao, nota);
    }

    public UsuarioId getAlunoId() {
        return alunoId;
    }

    public MatriculaStatus getStatus() {
        return status;
    }

    public Map<String, Nota> getAvaliacoes() {
        return Map.copyOf(avaliacoes);
    }
}