package dev.com.sigea.apresentacao.usuarios_admin.dto;

import java.util.List;

public class DisciplinaResponse {
    private Long id;
    private String codigo;
    private String nome;
    private String periodo;
    private String status;
    private Integer salasOfertadas;
    private List<Long> preRequisitosIds;
    
    public DisciplinaResponse() {
    }
    
    public DisciplinaResponse(Long id, String codigo, String nome, String periodo, String status, Integer salasOfertadas, List<Long> preRequisitosIds) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.periodo = periodo;
        this.status = status;
        this.salasOfertadas = salasOfertadas;
        this.preRequisitosIds = preRequisitosIds;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getSalasOfertadas() {
        return salasOfertadas;
    }
    
    public void setSalasOfertadas(Integer salasOfertadas) {
        this.salasOfertadas = salasOfertadas;
    }
    
    public List<Long> getPreRequisitosIds() {
        return preRequisitosIds;
    }
    
    public void setPreRequisitosIds(List<Long> preRequisitosIds) {
        this.preRequisitosIds = preRequisitosIds;
    }
}
