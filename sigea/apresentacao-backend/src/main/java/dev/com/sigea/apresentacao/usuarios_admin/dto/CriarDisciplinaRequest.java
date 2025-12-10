package dev.com.sigea.apresentacao.usuarios_admin.dto;

import java.util.List;

public class CriarDisciplinaRequest {
    private String nome;
    private String periodo;
    private List<Long> preRequisitos;
    
    public CriarDisciplinaRequest() {
    }
    
    public CriarDisciplinaRequest(String nome, String periodo, List<Long> preRequisitos) {
        this.nome = nome;
        this.periodo = periodo;
        this.preRequisitos = preRequisitos;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
    
    public List<Long> getPreRequisitos() {
        return preRequisitos;
    }
    
    public void setPreRequisitos(List<Long> preRequisitos) {
        this.preRequisitos = preRequisitos;
    }
}
