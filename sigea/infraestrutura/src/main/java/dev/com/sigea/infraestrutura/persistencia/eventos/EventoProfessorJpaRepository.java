package dev.com.sigea.infraestrutura.persistencia.eventos;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoProfessorJpaRepository extends JpaRepository<EventoProfessorEntity, Long> {
    List<EventoProfessorEntity> findByProfessorIdOrderByDataEventoDesc(Long professorId);
}
