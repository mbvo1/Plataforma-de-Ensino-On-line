package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaId;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import dev.com.sigea.infraestrutura.persistencia.SalaEntity;
import dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SalaRepositoryImpl implements SalaRepository {
    
    private final SalaJpaRepository salaJpaRepository;
    
    public SalaRepositoryImpl(SalaJpaRepository salaJpaRepository) {
        this.salaJpaRepository = salaJpaRepository;
    }
    
    @Override
    public Sala salvar(Sala sala) {
        // Para manter compatibilidade, não implementamos persistência completa aqui
        // A criação de salas é feita via SalaController diretamente
        return sala;
    }
    
    @Override
    public Optional<Sala> buscarPorId(SalaId id) {
        try {
            Long idLong = Long.parseLong(id.valor());
            return salaJpaRepository.findById(idLong)
                .map(this::toDomain);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public SalaId proximoId() {
        return new SalaId(UUID.randomUUID().toString());
    }
    
    @Override
    public List<Sala> listarTodas() {
        return salaJpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Sala> listarPorDisciplina(DisciplinaId disciplinaId) {
        try {
            Long discId = Long.parseLong(disciplinaId.valor());
            return salaJpaRepository.findByDisciplinaId(discId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }
    
    private Sala toDomain(SalaEntity entity) {
        return new Sala(
            new SalaId(entity.getId().toString()),
            new DisciplinaId(entity.getDisciplinaId().toString()),
            entity.getLimiteVagas() != null ? entity.getLimiteVagas() : 30,
            new UsuarioId(entity.getProfessorId().toString())
        );
    }
}
