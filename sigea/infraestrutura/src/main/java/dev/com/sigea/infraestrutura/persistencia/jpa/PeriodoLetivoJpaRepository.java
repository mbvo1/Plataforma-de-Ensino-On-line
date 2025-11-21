package dev.com.sigea.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoJpa, Long> {
    
    Optional<PeriodoLetivoJpa> findByIdentificador(String identificador);
}

