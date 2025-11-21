package dev.com.sigea.infraestrutura.persistencia.repositorios;

import dev.com.sigea.aplicacao.disciplina.DisciplinaRepositorioAplicacao;
import dev.com.sigea.aplicacao.disciplina.DisciplinaResumo;
import dev.com.sigea.aplicacao.disciplina.DisciplinaResumoExpandido;
import dev.com.sigea.infraestrutura.persistencia.jpa.DisciplinaJpa;
import dev.com.sigea.infraestrutura.persistencia.jpa.DisciplinaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DisciplinaRepositorioAplicacaoImpl implements DisciplinaRepositorioAplicacao {
    
    private final DisciplinaJpaRepository jpaRepository;
    
    public DisciplinaRepositorioAplicacaoImpl(DisciplinaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<DisciplinaResumo> buscarResumoPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::paraResumo);
    }
    
    @Override
    public Optional<DisciplinaResumoExpandido> buscarResumoExpandidoPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::paraResumoExpandido);
    }
    
    @Override
    public List<DisciplinaResumo> listarResumos() {
        return jpaRepository.findAll().stream()
            .map(this::paraResumo)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DisciplinaResumo> buscarPorNome(String nome) {
        return jpaRepository.findByNomeContainingIgnoreCase(nome).stream()
            .map(this::paraResumo)
            .collect(Collectors.toList());
    }
    
    private DisciplinaResumo paraResumo(DisciplinaJpa jpa) {
        return new DisciplinaResumo(
            jpa.getId(),
            jpa.getCodigoDisciplina(),
            jpa.getNome(),
            jpa.getDescricao()
        );
    }
    
    private DisciplinaResumoExpandido paraResumoExpandido(DisciplinaJpa jpa) {
        List<Long> preRequisitosIds = jpa.getPreRequisitos().stream()
            .map(DisciplinaJpa::getId)
            .collect(Collectors.toList());
        
        return new DisciplinaResumoExpandido(
            jpa.getId(),
            jpa.getCodigoDisciplina(),
            jpa.getNome(),
            jpa.getDescricao(),
            preRequisitosIds
        );
    }
}

