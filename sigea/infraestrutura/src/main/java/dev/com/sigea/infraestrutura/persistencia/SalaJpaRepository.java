package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaJpaRepository extends JpaRepository<SalaEntity, Long> {
    List<SalaEntity> findByDisciplinaId(Long disciplinaId);
    Long countByDisciplinaId(Long disciplinaId);
    Long countByDisciplinaIdAndStatus(Long disciplinaId, String status);
}
