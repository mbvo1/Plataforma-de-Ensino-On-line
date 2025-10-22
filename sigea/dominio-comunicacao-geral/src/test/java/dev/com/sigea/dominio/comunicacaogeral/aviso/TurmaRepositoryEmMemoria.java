package dev.com.sigea.dominio.comunicacaogeral.aviso;

import dev.com.sigea.dominio.ambientecolaborativo.turma.CodigoAcesso;
import dev.com.sigea.dominio.ambientecolaborativo.turma.Turma;
import dev.com.sigea.dominio.ambientecolaborativo.turma.TurmaId;
import dev.com.sigea.dominio.ambientecolaborativo.turma.TurmaRepository;

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
    public Optional<Turma> buscarPorCodigoDeAcesso(CodigoAcesso codigo) {
        return turmas.values().stream()
            .filter(turma -> turma.getCodigoAcesso().equals(codigo))
            .findFirst();
    }

    @Override
    public TurmaId proximoId() {
        return new TurmaId(UUID.randomUUID().toString());
    }
}