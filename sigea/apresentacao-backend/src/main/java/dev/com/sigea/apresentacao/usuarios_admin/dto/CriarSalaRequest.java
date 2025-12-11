package dev.com.sigea.apresentacao.usuarios_admin.dto;

import java.util.List;

public class CriarSalaRequest {
    private String identificador;
    private Long professorId;
    private List<String> diasSemana;
    private String horarioInicio;
    private String horarioFim;
    private Integer vagas;
    private Long disciplinaId;
    
    public CriarSalaRequest() {
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
    
    public Integer getVagas() {
        return vagas;
    }
    
    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }
    
    public Long getDisciplinaId() {
        return disciplinaId;
    }
    
    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }
}
