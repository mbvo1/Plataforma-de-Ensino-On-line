package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaEntity, Long> {
    Optional<DisciplinaEntity> findByCodigo(String codigo);
}
