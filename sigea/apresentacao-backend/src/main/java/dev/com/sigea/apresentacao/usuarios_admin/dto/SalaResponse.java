package dev.com.sigea.apresentacao.usuarios_admin.dto;

import java.util.List;

public class SalaResponse {
    private Long id;
    private String identificador;
    private Long professorId;
    private String professorNome;
    private List<String> diasSemana;
    private String horarioInicio;
    private String horarioFim;
    private Integer limiteVagas;
    private Integer vagasOcupadas;
    private String status;
    
    public SalaResponse() {
    }
    
    public SalaResponse(Long id, String identificador, Long professorId, String professorNome, 
                       List<String> diasSemana, String horarioInicio, String horarioFim,
                       Integer limiteVagas, Integer vagasOcupadas, String status) {
        this.id = id;
        this.identificador = identificador;
        this.professorId = professorId;
        this.professorNome = professorNome;
        this.diasSemana = diasSemana;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.limiteVagas = limiteVagas;
        this.vagasOcupadas = vagasOcupadas;
        this.status = status;
    }
    
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
    
    public Long getProfessorId() {
        return professorId;
    }
    
    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }
    
    public String getProfessorNome() {
        return professorNome;
    }
    
    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }
    
    public List<String> getDiasSemana() {
        return diasSemana;
    }
    
    public void setDiasSemana(List<String> diasSemana) {
        this.diasSemana = diasSemana;
    }
    
    public String getHorarioInicio() {
        return horarioInicio;
    }
    
    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }
    
    public String getHorarioFim() {
        return horarioFim;
    }
    
    public void setHorarioFim(String horarioFim) {
        this.horarioFim = horarioFim;
    }
    
    public Integer getLimiteVagas() {
        return limiteVagas;
    }
    
    public void setLimiteVagas(Integer limiteVagas) {
        this.limiteVagas = limiteVagas;
    }
    
    public Integer getVagasOcupadas() {
        return vagasOcupadas;
    }
    
    public void setVagasOcupadas(Integer vagasOcupadas) {
        this.vagasOcupadas = vagasOcupadas;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
