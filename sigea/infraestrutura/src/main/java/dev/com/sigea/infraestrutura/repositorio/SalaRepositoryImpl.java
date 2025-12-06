package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaId;
import dev.com.sigea.dominio.sala.SalaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SalaRepositoryImpl implements SalaRepository {
    
    private final Map<SalaId, Sala> salas = new HashMap<>();
    
    @Override
    public Sala salvar(Sala sala) {
        salas.put(sala.getId(), sala);
        return sala;
    }
    
    @Override
    public Optional<Sala> buscarPorId(SalaId id) {
        return Optional.ofNullable(salas.get(id));
    }
    
    @Override
    public SalaId proximoId() {
        return new SalaId(UUID.randomUUID().toString());
    }
    
    @Override
    public List<Sala> listarTodas() {
        return new ArrayList<>(salas.values());
    }
    
    @Override
    public List<Sala> listarPorDisciplina(DisciplinaId disciplinaId) {
        return salas.values().stream()
            .filter(sala -> sala.getDisciplinaId().equals(disciplinaId))
            .collect(Collectors.toList());
    }
}
