package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para AvaliacaoEntity.
 */
@Repository
public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoEntity, Long> {
    
    /**
     * Busca todas as avaliações de uma matrícula.
     */
    List<AvaliacaoEntity> findByMatriculaId(Long matriculaId);
    
    /**
     * Busca uma avaliação específica de uma matrícula.
     */
    Optional<AvaliacaoEntity> findByMatriculaIdAndNomeAvaliacao(Long matriculaId, String nomeAvaliacao);
}
