package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.util.Optional;

public interface TurmaRepository {
    void salvar(Turma turma);
    Optional<Turma> buscarPorId(TurmaId id);
    Optional<Turma> buscarPorCodigoDeAcesso(CodigoAcesso codigo);
    TurmaId proximoId();
}