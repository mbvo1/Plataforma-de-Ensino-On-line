package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeriodoJpaRepository extends JpaRepository<PeriodoEntity, Long> {
    Optional<PeriodoEntity> findByStatus(String status);
}
