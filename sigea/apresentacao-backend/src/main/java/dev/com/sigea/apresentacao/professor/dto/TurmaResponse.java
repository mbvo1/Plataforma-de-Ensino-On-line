package dev.com.sigea.apresentacao.professor.dto;

public class TurmaResponse {
    private Long turmaId;
    private String titulo;
    private String nomeProfessor;
    
    public TurmaResponse() {
    }
    
    public TurmaResponse(Long turmaId, String titulo, String nomeProfessor) {
        this.turmaId = turmaId;
        this.titulo = titulo;
        this.nomeProfessor = nomeProfessor;
    }
    
    public Long getTurmaId() {
        return turmaId;
    }
    
    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getNomeProfessor() {
        return nomeProfessor;
    }
    
    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
    }
}
