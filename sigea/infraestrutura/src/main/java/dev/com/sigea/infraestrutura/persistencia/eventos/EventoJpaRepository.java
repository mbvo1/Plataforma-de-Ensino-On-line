package dev.com.sigea.infraestrutura.persistencia.eventos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoJpaRepository extends JpaRepository<EventoEntity, Long> {
    
    // Busca eventos entre duas datas
    List<EventoEntity> findByDataEventoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // Busca eventos por tipo
    List<EventoEntity> findByTipo(String tipo);
    
    // Busca eventos por responsável
    List<EventoEntity> findByResponsavelId(Long responsavelId);
}
