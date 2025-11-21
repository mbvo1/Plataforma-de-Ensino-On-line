package dev.com.sigea.infraestrutura.persistencia.repositorios;

import dev.com.sigea.dominio.gestaoacademica.disciplina.Disciplina;
import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaId;
import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaRepository;
import dev.com.sigea.infraestrutura.persistencia.jpa.DisciplinaJpa;
import dev.com.sigea.infraestrutura.persistencia.jpa.DisciplinaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DisciplinaRepositorioImpl implements DisciplinaRepository {
    
    private final DisciplinaJpaRepository jpaRepository;
    
    public DisciplinaRepositorioImpl(DisciplinaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void salvar(Disciplina disciplina) {
        Long id = disciplina.getId() != null && !disciplina.getId().valor().isEmpty() 
            ? Long.parseLong(disciplina.getId().valor()) 
            : null;
        
        DisciplinaJpa jpa = id != null && jpaRepository.existsById(id)
            ? jpaRepository.findById(id).orElse(new DisciplinaJpa())
            : new DisciplinaJpa();
        
        if (jpa.getId() == null && id != null) {
            jpa.setId(id);
        }
        jpa.setNome(disciplina.getNome());
        
        jpaRepository.save(jpa);
    }
    
    @Override
    public Optional<Disciplina> buscarPorNome(String nome) {
        return jpaRepository.findByNome(nome)
            .map(this::paraDominio);
    }
    
    @Override
    public DisciplinaId proximoId() {
        return new DisciplinaId(String.valueOf(System.currentTimeMillis()));
    }
    
    @Override
    public List<Disciplina> listarTodas() {
        return jpaRepository.findAll().stream()
            .map(this::paraDominio)
            .collect(Collectors.toList());
    }
    
    private Disciplina paraDominio(DisciplinaJpa jpa) {
        DisciplinaId id = new DisciplinaId(String.valueOf(jpa.getId()));
        return new Disciplina(id, jpa.getNome());
    }
}

