package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidade JPA para a tabela Chamadas.
 * Armazena presença/falta dos alunos por data e aula.
 */
@Entity
@Table(name = "Chamadas")
public class ChamadaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chamada_id")
    private Long id;
    
    @Column(name = "sala_id", nullable = false)
    private Long salaId;
    
    @Column(name = "matricula_id", nullable = false)
    private Long matriculaId;
    
    @Column(name = "data_chamada", nullable = false)
    private LocalDate dataChamada;
    
    @Column(name = "falta_aula1", nullable = false)
    private Boolean faltaAula1 = false;
    
    @Column(name = "falta_aula2", nullable = false)
    private Boolean faltaAula2 = false;
    
    public ChamadaEntity() {
    }
    
    public ChamadaEntity(Long salaId, Long matriculaId, LocalDate dataChamada, Boolean faltaAula1, Boolean faltaAula2) {
        this.salaId = salaId;
        this.matriculaId = matriculaId;
        this.dataChamada = dataChamada;
        this.faltaAula1 = faltaAula1;
        this.faltaAula2 = faltaAula2;
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
    
    public Long getMatriculaId() {
        return matriculaId;
    }
    
    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }
    
    public LocalDate getDataChamada() {
        return dataChamada;
    }
    
    public void setDataChamada(LocalDate dataChamada) {
        this.dataChamada = dataChamada;
    }
    
    public Boolean getFaltaAula1() {
        return faltaAula1;
    }
    
    public void setFaltaAula1(Boolean faltaAula1) {
        this.faltaAula1 = faltaAula1;
    }
    
    public Boolean getFaltaAula2() {
        return faltaAula2;
    }
    
    public void setFaltaAula2(Boolean faltaAula2) {
        this.faltaAula2 = faltaAula2;
    }
}

