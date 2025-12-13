package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para RespostaEntity.
 * Gerencia a persistência de respostas/comentários em tópicos.
 */
@Repository
public interface RespostaJpaRepository extends JpaRepository<RespostaEntity, Long> {
    
    /**
     * Busca todas as respostas de um tópico ordenadas por data de criação.
     */
    List<RespostaEntity> findByTopicoIdOrderByDataCriacaoAsc(Long topicoId);
    
    /**
     * Conta quantas respostas existem para um tópico.
     */
    long countByTopicoId(Long topicoId);
}
