package dev.com.sigea.infraestrutura.persistencia.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Disciplinas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaJpa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disciplina_id")
    private Long id;
    
    @Column(name = "codigo_disciplina", nullable = false, unique = true)
    private String codigoDisciplina;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @ManyToMany
    @JoinTable(
        name = "Disciplina_PreRequisitos",
        joinColumns = @JoinColumn(name = "disciplina_id"),
        inverseJoinColumns = @JoinColumn(name = "pre_requisito_id")
    )
    private List<DisciplinaJpa> preRequisitos = new ArrayList<>();
}

