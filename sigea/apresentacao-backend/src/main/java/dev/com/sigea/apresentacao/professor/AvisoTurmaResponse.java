package dev.com.sigea.apresentacao.professor;

public class AvisoTurmaResponse {
    
    private Long avisoId;
    private String professorNome;
    private String mensagem;
    private String arquivoPath;
    private String dataPostagem;
    
    public AvisoTurmaResponse() {
    }
    
    public AvisoTurmaResponse(Long avisoId, String professorNome, String mensagem, 
                              String arquivoPath, String dataPostagem) {
        this.avisoId = avisoId;
        this.professorNome = professorNome;
        this.mensagem = mensagem;
        this.arquivoPath = arquivoPath;
        this.dataPostagem = dataPostagem;
    }
    
    // Getters and Setters
    public Long getAvisoId() {
        return avisoId;
    }
    
    public void setAvisoId(Long avisoId) {
        this.avisoId = avisoId;
    }
    
    public String getProfessorNome() {
        return professorNome;
    }
    
    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }
    
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
    
    public String getDataPostagem() {
        return dataPostagem;
    }
    
    public void setDataPostagem(String dataPostagem) {
        this.dataPostagem = dataPostagem;
    }
}
