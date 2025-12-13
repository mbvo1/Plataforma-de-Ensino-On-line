package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicoJpaRepository extends JpaRepository<TopicoEntity, Long> {
    
    List<TopicoEntity> findByDisciplinaIdOrderByIdDesc(Long disciplinaId);
}
