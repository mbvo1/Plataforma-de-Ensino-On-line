package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para MatriculaEntity.
 */
@Repository
public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, Long> {
    
    /**
     * Busca todas as matrículas de uma sala.
     */
    List<MatriculaEntity> findBySalaId(Long salaId);
    
    /**
     * Busca todas as matrículas ativas de uma sala.
     */
    List<MatriculaEntity> findBySalaIdAndStatus(Long salaId, String status);
    
    /**
     * Busca matrícula de um aluno em uma sala.
     */
    MatriculaEntity findBySalaIdAndAlunoId(Long salaId, Long alunoId);
    
    /**
     * Conta alunos matriculados em uma sala.
     */
    long countBySalaIdAndStatus(Long salaId, String status);
}
