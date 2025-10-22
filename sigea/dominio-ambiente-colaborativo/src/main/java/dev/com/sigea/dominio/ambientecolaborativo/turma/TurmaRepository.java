package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.util.Optional;

public interface TurmaRepository {
    void salvar(Turma turma);
    Optional<Turma> buscarPorId(TurmaId id);
    TurmaId proximoId();
}