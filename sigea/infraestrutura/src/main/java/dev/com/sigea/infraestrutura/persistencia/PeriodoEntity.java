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
    
    @Column(name = "data_inicio")
    private java.time.LocalDate dataInicio;
    
    @Column(name = "data_fim")
    private java.time.LocalDate dataFim;
    
    @Column(name = "inscricao_inicio")
    private java.time.LocalDate inscricaoInicio;
    
    @Column(name = "inscricao_fim")
    private java.time.LocalDate inscricaoFim;
    
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
    
    public java.time.LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(java.time.LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public java.time.LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(java.time.LocalDate dataFim) {
        this.dataFim = dataFim;
    }
    
    public java.time.LocalDate getInscricaoInicio() {
        return inscricaoInicio;
    }
    
    public void setInscricaoInicio(java.time.LocalDate inscricaoInicio) {
        this.inscricaoInicio = inscricaoInicio;
    }
    
    public java.time.LocalDate getInscricaoFim() {
        return inscricaoFim;
    }
    
    public void setInscricaoFim(java.time.LocalDate inscricaoFim) {
        this.inscricaoFim = inscricaoFim;
    }
}
