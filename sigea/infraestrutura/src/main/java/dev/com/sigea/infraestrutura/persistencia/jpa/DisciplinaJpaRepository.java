package dev.com.sigea.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaJpa, Long> {
    
    Optional<DisciplinaJpa> findByNome(String nome);
    
    List<DisciplinaJpa> findByNomeContainingIgnoreCase(String nome);
}

