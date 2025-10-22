package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TurmaRepositoryEmMemoria implements TurmaRepository {

    private final Map<TurmaId, Turma> turmas = new HashMap<>();

    @Override
    public void salvar(Turma turma) {
        turmas.put(turma.getId(), turma);
    }

    @Override
    public Optional<Turma> buscarPorId(TurmaId id) {
        return Optional.ofNullable(turmas.get(id));
    }

    @Override
    public TurmaId proximoId() {
        return new TurmaId(UUID.randomUUID().toString());
    }
}