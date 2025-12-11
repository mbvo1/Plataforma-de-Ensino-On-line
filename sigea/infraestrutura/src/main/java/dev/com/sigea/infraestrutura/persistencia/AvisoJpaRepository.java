package dev.com.sigea.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoJpaRepository extends JpaRepository<AvisoEntity, Long> {
    List<AvisoEntity> findByAlvoTipo(String alvoTipo);
    List<AvisoEntity> findByAutorId(Long autorId);
}
