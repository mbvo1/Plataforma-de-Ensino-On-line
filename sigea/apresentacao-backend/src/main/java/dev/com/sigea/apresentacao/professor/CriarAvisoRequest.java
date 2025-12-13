package dev.com.sigea.apresentacao.professor;

public class CriarAvisoRequest {
    
    private String mensagem;
    private String arquivoPath;
    private String arquivoConteudo;
    
    // Constructors
    public CriarAvisoRequest() {}
    
    public CriarAvisoRequest(String mensagem, String arquivoPath, String arquivoConteudo) {
        this.mensagem = mensagem;
        this.arquivoPath = arquivoPath;
        this.arquivoConteudo = arquivoConteudo;
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
    
    public String getArquivoConteudo() {
        return arquivoConteudo;
    }
    
    public void setArquivoConteudo(String arquivoConteudo) {
        this.arquivoConteudo = arquivoConteudo;
    }
}
