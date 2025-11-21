package dev.com.sigea.infraestrutura.persistencia.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "PeriodosLetivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoLetivoJpa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "periodo_letivo_id")
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String identificador;
    
    @Column(name = "data_inicio", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataInicio;
    
    @Column(name = "data_fim", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataFim;
    
    @Column(name = "data_inicio_matricula", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataInicioMatricula;
    
    @Column(name = "data_fim_matricula", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataFimMatricula;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodoStatusJpa status;
    
    public enum PeriodoStatusJpa {
        ATIVO, ENCERRADO, MATRICULAS_ABERTAS
    }
}

