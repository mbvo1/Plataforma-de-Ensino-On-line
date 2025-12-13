package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidade JPA para a tabela Matriculas.
 */
@Entity
@Table(name = "Matriculas")
public class MatriculaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matricula_id")
    private Long id;
    
    @Column(name = "sala_id", nullable = false)
    private Long salaId;
    
    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;
    
    @Column(name = "data_matricula", nullable = false)
    private LocalDate dataMatricula;
    
    @Column(name = "total_faltas", nullable = false)
    private Integer totalFaltas = 0;
    
    @Column(name = "status", nullable = false)
    private String status = "ATIVA";
    
    @Column(name = "situacao", nullable = false)
    private String situacao = "CURSANDO";
    
    public MatriculaEntity() {
    }
    
    public MatriculaEntity(Long salaId, Long alunoId) {
        this.salaId = salaId;
        this.alunoId = alunoId;
        this.dataMatricula = LocalDate.now();
        this.totalFaltas = 0;
        this.status = "ATIVA";
        this.situacao = "CURSANDO";
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSalaId() {
        return salaId;
    }
    
    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }
    
    public Long getAlunoId() {
        return alunoId;
    }
    
    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }
    
    public LocalDate getDataMatricula() {
        return dataMatricula;
    }
    
    public void setDataMatricula(LocalDate dataMatricula) {
        this.dataMatricula = dataMatricula;
    }
    
    public Integer getTotalFaltas() {
        return totalFaltas;
    }
    
    public void setTotalFaltas(Integer totalFaltas) {
        this.totalFaltas = totalFaltas;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSituacao() {
        return situacao;
    }
    
    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
