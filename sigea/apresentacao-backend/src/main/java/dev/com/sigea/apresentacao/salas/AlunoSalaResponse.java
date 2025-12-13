package dev.com.sigea.apresentacao.salas;

/**
 * DTO para resposta de aluno matriculado em uma sala.
 */
public class AlunoSalaResponse {
    
    private Long matriculaId;
    private Long alunoId;
    private String nome;
    private Double notaAv1;
    private Double notaAv2;
    private Integer totalFaltas;
    
    public AlunoSalaResponse() {
    }
    
    // Getters e Setters
    
    public Long getMatriculaId() {
        return matriculaId;
    }
    
    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }
    
    public Long getAlunoId() {
        return alunoId;
    }
    
    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public Double getNotaAv1() {
        return notaAv1;
    }
    
    public void setNotaAv1(Double notaAv1) {
        this.notaAv1 = notaAv1;
    }
    
    public Double getNotaAv2() {
        return notaAv2;
    }
    
    public void setNotaAv2(Double notaAv2) {
        this.notaAv2 = notaAv2;
    }
    
    public Integer getTotalFaltas() {
        return totalFaltas;
    }
    
    public void setTotalFaltas(Integer totalFaltas) {
        this.totalFaltas = totalFaltas;
    }
}
