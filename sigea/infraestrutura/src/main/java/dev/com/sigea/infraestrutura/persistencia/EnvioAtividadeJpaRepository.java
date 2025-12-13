package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioAtividadeJpaRepository extends JpaRepository<EnvioAtividadeEntity, Long> {
    
    Optional<EnvioAtividadeEntity> findByAtividadeIdAndAlunoId(Long atividadeId, Long alunoId);
    
    List<EnvioAtividadeEntity> findByAtividadeId(Long atividadeId);
    
    List<EnvioAtividadeEntity> findByAlunoId(Long alunoId);
    
    boolean existsByAtividadeIdAndAlunoId(Long atividadeId, Long alunoId);
    
    long countByAtividadeId(Long atividadeId);
}
