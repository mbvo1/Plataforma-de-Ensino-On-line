package dev.com.sigea.apresentacao.professor;

public class CriarAvisoTurmaRequest {
    
    private String mensagem;
    private String nomeArquivo;
    
    public CriarAvisoTurmaRequest() {
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
}
