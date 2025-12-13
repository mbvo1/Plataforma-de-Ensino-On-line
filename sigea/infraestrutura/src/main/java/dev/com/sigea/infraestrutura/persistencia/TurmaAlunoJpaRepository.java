package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaAlunoJpaRepository extends JpaRepository<TurmaAlunoEntity, TurmaAlunoId> {
    
    List<TurmaAlunoEntity> findByAlunoId(Long alunoId);
    
    List<TurmaAlunoEntity> findByTurmaId(Long turmaId);
    
    boolean existsByTurmaIdAndAlunoId(Long turmaId, Long alunoId);
}
