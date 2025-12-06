package dev.com.sigea.dominio.forum;

import dev.com.sigea.dominio.disciplina.DisciplinaId;

import java.util.List;
import java.util.Optional;

public interface TopicoRepository {
    void salvar(Topico topico);
    Optional<Topico> buscarPorId(TopicoId id);
    TopicoId proximoId();
    List<Topico> listarPorDisciplina(DisciplinaId disciplinaId);
}