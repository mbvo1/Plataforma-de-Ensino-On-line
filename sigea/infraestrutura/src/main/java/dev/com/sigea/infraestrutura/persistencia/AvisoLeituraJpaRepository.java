package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisoLeituraJpaRepository extends JpaRepository<AvisoLeituraEntity, Long> {
    
    Optional<AvisoLeituraEntity> findByAvisoIdAndUsuarioId(Long avisoId, Long usuarioId);
    
    @Query("SELECT l.avisoId FROM AvisoLeituraEntity l WHERE l.usuarioId = :usuarioId")
    List<Long> findAvisosLidosByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    boolean existsByAvisoIdAndUsuarioId(Long avisoId, Long usuarioId);
}
