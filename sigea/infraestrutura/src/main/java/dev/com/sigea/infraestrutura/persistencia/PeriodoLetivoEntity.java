package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PeriodosLetivos")
public class PeriodoLetivoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "periodo_letivo_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String identificador;
    
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;
    
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;
    
    @Column(name = "data_inicio_matricula", nullable = false)
    private LocalDate dataInicioMatricula;
    
    @Column(name = "data_fim_matricula", nullable = false)
    private LocalDate dataFimMatricula;
    
    @Column(nullable = false, length = 50)
    private String status = "ABERTO";
    
    public PeriodoLetivoEntity() {
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIdentificador() {
        return identificador;
    }
    
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
    
    public LocalDate getDataInicioMatricula() {
        return dataInicioMatricula;
    }
    
    public void setDataInicioMatricula(LocalDate dataInicioMatricula) {
        this.dataInicioMatricula = dataInicioMatricula;
    }
    
    public LocalDate getDataFimMatricula() {
        return dataFimMatricula;
    }
    
    public void setDataFimMatricula(LocalDate dataFimMatricula) {
        this.dataFimMatricula = dataFimMatricula;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
