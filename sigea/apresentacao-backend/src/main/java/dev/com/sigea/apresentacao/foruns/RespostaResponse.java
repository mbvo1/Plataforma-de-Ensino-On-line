package dev.com.sigea.apresentacao.foruns;

/**
 * DTO para retorno de resposta/comentário.
 */
public class RespostaResponse {
    
    private Long id;
    private Long topicoId;
    private Long autorId;
    private String nomeAutor;
    private String conteudo;
    private Boolean verificadaPeloProfessor;
    private String dataCriacao;
    
    public RespostaResponse() {
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTopicoId() {
        return topicoId;
    }
    
    public void setTopicoId(Long topicoId) {
        this.topicoId = topicoId;
    }
    
    public Long getAutorId() {
        return autorId;
    }
    
    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }
    
    public String getNomeAutor() {
        return nomeAutor;
    }
    
    public void setNomeAutor(String nomeAutor) {
        this.nomeAutor = nomeAutor;
    }
    
    public String getConteudo() {
        return conteudo;
    }
    
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public Boolean getVerificadaPeloProfessor() {
        return verificadaPeloProfessor;
    }
    
    public void setVerificadaPeloProfessor(Boolean verificadaPeloProfessor) {
        this.verificadaPeloProfessor = verificadaPeloProfessor;
    }
    
    public String getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
