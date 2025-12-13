package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoEntity, Long> {
    
    List<PeriodoLetivoEntity> findByStatus(String status);
    
    PeriodoLetivoEntity findByIdentificador(String identificador);
}
