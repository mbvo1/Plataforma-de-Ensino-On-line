package dev.com.sigea.dominio.sala;

import dev.com.sigea.dominio.disciplina.DisciplinaId;

import java.util.List; 
import java.util.Optional;

public interface SalaRepository {
    Sala salvar(Sala sala);
    Optional<Sala> buscarPorId(SalaId id);
    SalaId proximoId();

    List<Sala> listarTodas();
    
    /**
     * Lista todas as salas de uma disciplina específica
     */
    List<Sala> listarPorDisciplina(DisciplinaId disciplinaId);
}