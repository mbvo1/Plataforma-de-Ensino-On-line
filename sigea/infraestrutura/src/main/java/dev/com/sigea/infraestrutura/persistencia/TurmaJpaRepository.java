package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaJpaRepository extends JpaRepository<TurmaEntity, Long> {
    List<TurmaEntity> findByProfessorCriadorId(Long professorId);
}
