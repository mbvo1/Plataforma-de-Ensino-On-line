package dev.com.sigea.apresentacao.professor;

public class CriarAvisoRequest {
    
    private String mensagem;
    private String arquivoPath;
    
    // Constructors
    public CriarAvisoRequest() {}
    
    public CriarAvisoRequest(String mensagem, String arquivoPath) {
        this.mensagem = mensagem;
        this.arquivoPath = arquivoPath;
    }
    
    // Getters and Setters
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public String getArquivoPath() {
        return arquivoPath;
    }
    
    public void setArquivoPath(String arquivoPath) {
        this.arquivoPath = arquivoPath;
    }
}
