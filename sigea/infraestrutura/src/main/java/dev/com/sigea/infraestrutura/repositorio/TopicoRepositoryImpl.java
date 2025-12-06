package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.forum.TopicoId;
import dev.com.sigea.dominio.forum.TopicoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TopicoRepositoryImpl implements TopicoRepository {
    
    private final Map<TopicoId, Topico> topicos = new HashMap<>();
    
    @Override
    public void salvar(Topico topico) {
        topicos.put(topico.getId(), topico);
    }
    
    @Override
    public Optional<Topico> buscarPorId(TopicoId id) {
        return Optional.ofNullable(topicos.get(id));
    }
    
    @Override
    public TopicoId proximoId() {
        return new TopicoId(UUID.randomUUID().toString());
    }
    
    @Override
    public List<Topico> listarPorDisciplina(DisciplinaId disciplinaId) {
        return topicos.values().stream()
            .filter(topico -> topico.getDisciplinaId().equals(disciplinaId))
            .collect(Collectors.toList());
    }
}
