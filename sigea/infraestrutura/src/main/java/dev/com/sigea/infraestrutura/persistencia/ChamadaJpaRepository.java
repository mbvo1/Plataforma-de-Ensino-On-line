package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade ChamadaEntity.
 */
public interface ChamadaJpaRepository extends JpaRepository<ChamadaEntity, Long> {
    
    /**
     * Busca todas as chamadas de uma sala em uma data específica.
     */
    List<ChamadaEntity> findBySalaIdAndDataChamada(Long salaId, LocalDate dataChamada);
    
    /**
     * Busca a chamada de um aluno específico em uma data.
     */
    Optional<ChamadaEntity> findBySalaIdAndMatriculaIdAndDataChamada(
        Long salaId, 
        Long matriculaId, 
        LocalDate dataChamada
    );
    
    /**
     * Busca todas as chamadas de uma matrícula (histórico do aluno).
     */
    List<ChamadaEntity> findByMatriculaIdOrderByDataChamadaDesc(Long matriculaId);
    
    /**
     * Busca todas as chamadas de uma matrícula em uma sala específica.
     */
    List<ChamadaEntity> findByMatriculaIdAndSalaId(Long matriculaId, Long salaId);
}

