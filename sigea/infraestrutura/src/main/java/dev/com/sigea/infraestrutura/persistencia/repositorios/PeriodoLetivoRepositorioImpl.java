package dev.com.sigea.infraestrutura.persistencia.repositorios;

import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivo;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivoId;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivoRepository;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoStatus;
import dev.com.sigea.infraestrutura.persistencia.jpa.PeriodoLetivoJpa;
import dev.com.sigea.infraestrutura.persistencia.jpa.PeriodoLetivoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PeriodoLetivoRepositorioImpl implements PeriodoLetivoRepository {
    
    private final PeriodoLetivoJpaRepository jpaRepository;
    
    public PeriodoLetivoRepositorioImpl(PeriodoLetivoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void salvar(PeriodoLetivo periodoLetivo) {
        Long id = periodoLetivo.getId() != null && !periodoLetivo.getId().valor().isEmpty()
            ? Long.parseLong(periodoLetivo.getId().valor())
            : null;
        
        PeriodoLetivoJpa jpa = id != null && jpaRepository.existsById(id)
            ? jpaRepository.findById(id).orElse(new PeriodoLetivoJpa())
            : new PeriodoLetivoJpa();
        
        if (jpa.getId() == null && id != null) {
            jpa.setId(id);
        }
        jpa.setIdentificador(periodoLetivo.getIdentificador());
        jpa.setDataInicio(periodoLetivo.getDataInicio());
        jpa.setDataFim(periodoLetivo.getDataFim());
        jpa.setStatus(PeriodoLetivoJpa.PeriodoStatusJpa.valueOf(periodoLetivo.getStatus().name()));
        
        // Para datas de matrícula, usar as mesmas datas do período por enquanto
        // (o domínio não tem esses campos, mas o schema SQL exige)
        if (jpa.getDataInicioMatricula() == null) {
            jpa.setDataInicioMatricula(periodoLetivo.getDataInicio());
        }
        if (jpa.getDataFimMatricula() == null) {
            jpa.setDataFimMatricula(periodoLetivo.getDataFim());
        }
        
        jpaRepository.save(jpa);
    }
    
    @Override
    public Optional<PeriodoLetivo> buscarPorIdentificador(String identificador) {
        return jpaRepository.findByIdentificador(identificador)
            .map(this::paraDominio);
    }
    
    @Override
    public PeriodoLetivoId proximoId() {
        return new PeriodoLetivoId(String.valueOf(System.currentTimeMillis()));
    }
    
    @Override
    public List<PeriodoLetivo> listarTodas() {
        return jpaRepository.findAll().stream()
            .map(this::paraDominio)
            .collect(Collectors.toList());
    }
    
    private PeriodoLetivo paraDominio(PeriodoLetivoJpa jpa) {
        PeriodoLetivoId id = new PeriodoLetivoId(String.valueOf(jpa.getId()));
        PeriodoLetivo periodo = new PeriodoLetivo(
            id,
            jpa.getIdentificador(),
            jpa.getDataInicio(),
            jpa.getDataFim()
        );
        return periodo;
    }
}

