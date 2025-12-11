package dev.com.sigea.apresentacao.usuarios_admin.dto;

public class PeriodoResponse {
    private Long id;
    private String nome;
    private String status;
    private String dataInicio;
    private String dataFim;
    private String dataInicioInscricao;
    private String dataFimInscricao;
    
    public PeriodoResponse() {
    }
    
    public PeriodoResponse(Long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }
    
    public PeriodoResponse(Long id, String nome, String status, String dataInicio, String dataFim, 
                          String dataInicioInscricao, String dataFimInscricao) {
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.dataInicioInscricao = dataInicioInscricao;
        this.dataFimInscricao = dataFimInscricao;
    }
    
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
    
    public String getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public String getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(String dataFim) {
        this.dataFim = dataFim;
    }
    
    public String getDataInicioInscricao() {
        return dataInicioInscricao;
    }
    
    public void setDataInicioInscricao(String dataInicioInscricao) {
        this.dataInicioInscricao = dataInicioInscricao;
    }
    
    public String getDataFimInscricao() {
        return dataFimInscricao;
    }
    
    public void setDataFimInscricao(String dataFimInscricao) {
        this.dataFimInscricao = dataFimInscricao;
    }
}
