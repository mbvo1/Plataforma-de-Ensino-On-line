package dev.com.sigea.infraestrutura.colaborativo;

import dev.com.sigea.dominio.colaborativo.AvisoTurmaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoTurmaJpaRepository extends JpaRepository<AvisoTurmaEntity, Long> {
    
    List<AvisoTurmaEntity> findByTurmaIdOrderByDataPostagemDesc(Long turmaId);
}
