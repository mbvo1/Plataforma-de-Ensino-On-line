package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.forum.TopicoId;
import dev.com.sigea.dominio.forum.TopicoRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import dev.com.sigea.infraestrutura.persistencia.TopicoEntity;
import dev.com.sigea.infraestrutura.persistencia.TopicoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TopicoRepositoryImpl implements TopicoRepository {
    
    private final TopicoJpaRepository topicoJpaRepository;
    
    public TopicoRepositoryImpl(TopicoJpaRepository topicoJpaRepository) {
        this.topicoJpaRepository = topicoJpaRepository;
    }
    
    @Override
    public void salvar(Topico topico) {
        TopicoEntity entity = new TopicoEntity(
            Long.parseLong(topico.getDisciplinaId().valor()),
            Long.parseLong(topico.getAutorId().valor()),
            topico.getTitulo(),
            topico.getConteudo(),
            topico.getArquivoPath()
        );
        topicoJpaRepository.save(entity);
    }
    
    @Override
    public Optional<Topico> buscarPorId(TopicoId id) {
        return topicoJpaRepository.findById(Long.parseLong(id.valor()))
            .map(this::toDomain);
    }
    
    @Override
    public TopicoId proximoId() {
        return new TopicoId(UUID.randomUUID().toString());
    }
    
    @Override
    public List<Topico> listarPorDisciplina(DisciplinaId disciplinaId) {
        return topicoJpaRepository.findByDisciplinaIdOrderByIdDesc(Long.parseLong(disciplinaId.valor()))
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void excluir(TopicoId id) {
        topicoJpaRepository.deleteById(Long.parseLong(id.valor()));
    }
    
    private Topico toDomain(TopicoEntity entity) {
        return new Topico(
            new TopicoId(entity.getId().toString()),
            new DisciplinaId(entity.getDisciplinaId().toString()),
            entity.getTitulo(),
            entity.getConteudo(),
            new UsuarioId(entity.getAutorId().toString()),
            entity.getArquivoPath()
        );
    }
}
