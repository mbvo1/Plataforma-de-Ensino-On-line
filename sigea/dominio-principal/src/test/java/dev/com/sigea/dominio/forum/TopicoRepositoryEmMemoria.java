package dev.com.sigea.dominio.forum;

import dev.com.sigea.dominio.disciplina.DisciplinaId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TopicoRepositoryEmMemoria implements TopicoRepository {
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
    
    @Override
    public void excluir(TopicoId id) {
        topicos.remove(id);
    }
}

