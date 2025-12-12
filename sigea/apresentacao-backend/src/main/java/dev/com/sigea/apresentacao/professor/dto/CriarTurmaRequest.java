package dev.com.sigea.apresentacao.professor.dto;

public class CriarTurmaRequest {
    private String nomeTurma;
    private String codigoAcesso;
    
    public CriarTurmaRequest() {
    }
    
    public String getNomeTurma() {
        return nomeTurma;
    }
    
    public void setNomeTurma(String nomeTurma) {
        this.nomeTurma = nomeTurma;
    }
    
    public String getCodigoAcesso() {
        return codigoAcesso;
    }
    
    public void setCodigoAcesso(String codigoAcesso) {
        this.codigoAcesso = codigoAcesso;
    }
}
