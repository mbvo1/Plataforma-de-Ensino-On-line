package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "periodos")
public class PeriodoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 10)
    private String nome;
    
    @Column(nullable = false, length = 20)
    private String status = "ATIVO";
    
    public PeriodoEntity() {
    }
    
    public PeriodoEntity(Long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
