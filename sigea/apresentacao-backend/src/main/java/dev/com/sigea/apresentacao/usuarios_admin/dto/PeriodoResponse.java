package dev.com.sigea.apresentacao.usuarios_admin.dto;

public class PeriodoResponse {
    private Long id;
    private String nome;
    private String status;
    
    public PeriodoResponse() {
    }
    
    public PeriodoResponse(Long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
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
}
