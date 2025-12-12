package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "Turmas")
public class TurmaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turma_id")
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String titulo;
    
    @Column(name = "codigo_acesso", length = 50, unique = true)
    private String codigoAcesso;
    
    @Column(name = "professor_criador_id", nullable = false)
    private Long professorCriadorId;
    
    public TurmaEntity() {
    }
    
    public TurmaEntity(Long id, String titulo, String codigoAcesso, Long professorCriadorId) {
        this.id = id;
        this.titulo = titulo;
        this.codigoAcesso = codigoAcesso;
        this.professorCriadorId = professorCriadorId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getCodigoAcesso() {
        return codigoAcesso;
    }
    
    public void setCodigoAcesso(String codigoAcesso) {
        this.codigoAcesso = codigoAcesso;
    }
    
    public Long getProfessorCriadorId() {
        return professorCriadorId;
    }
    
    public void setProfessorCriadorId(Long professorCriadorId) {
        this.professorCriadorId = professorCriadorId;
    }
}
