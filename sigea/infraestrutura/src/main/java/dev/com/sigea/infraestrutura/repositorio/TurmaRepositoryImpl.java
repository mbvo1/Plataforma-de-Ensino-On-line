package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.turma.CodigoAcesso;
import dev.com.sigea.dominio.turma.Turma;
import dev.com.sigea.dominio.turma.TurmaId;
import dev.com.sigea.dominio.turma.TurmaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TurmaRepositoryImpl implements TurmaRepository {
    
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
