package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "Salas")
public class SalaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sala_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String identificador;
    
    @Column(name = "disciplina_id", nullable = false)
    private Long disciplinaId;
    
    @Column(name = "periodo_letivo_id")
    private Long periodoLetivoId;
    
    @Column(name = "professor_responsavel_id", nullable = false)
    private Long professorId;
    
    @Column(name = "horario", length = 255)
    private String horario; // Formato: "SEG,QUA 08:30-10:30"
    
    @Column(name = "limite_de_vagas", nullable = false)
    private Integer limiteVagas;
    
    @Column(length = 20)
    private String status = "ATIVO";
    
    public SalaEntity() {
    }
    
    public SalaEntity(Long id, String identificador, Long disciplinaId, Long periodoLetivoId,
                      Long professorId, String horario, Integer limiteVagas, String status) {
        this.id = id;
        this.identificador = identificador;
        this.disciplinaId = disciplinaId;
        this.periodoLetivoId = periodoLetivoId;
        this.professorId = professorId;
        this.horario = horario;
        this.limiteVagas = limiteVagas;
        this.status = status;
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
    
    public Long getDisciplinaId() {
        return disciplinaId;
    }
    
    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }
    
    public Long getPeriodoLetivoId() {
        return periodoLetivoId;
    }
    
    public void setPeriodoLetivoId(Long periodoLetivoId) {
        this.periodoLetivoId = periodoLetivoId;
    }
    
    public Long getProfessorId() {
        return professorId;
    }
    
    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }
    
    public String getHorario() {
        return horario;
    }
    
    public void setHorario(String horario) {
        this.horario = horario;
    }
    
    public Integer getLimiteVagas() {
        return limiteVagas;
    }
    
    public void setLimiteVagas(Integer limiteVagas) {
        this.limiteVagas = limiteVagas;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
