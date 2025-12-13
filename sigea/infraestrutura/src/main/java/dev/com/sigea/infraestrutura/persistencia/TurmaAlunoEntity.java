package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "Turma_Alunos")
@IdClass(TurmaAlunoId.class)
public class TurmaAlunoEntity {
    
    @Id
    @Column(name = "turma_id")
    private Long turmaId;
    
    @Id
    @Column(name = "aluno_id")
    private Long alunoId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", insertable = false, updatable = false)
    private TurmaEntity turma;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", insertable = false, updatable = false)
    private UsuarioEntity aluno;
    
    public TurmaAlunoEntity() {
    }
    
    public TurmaAlunoEntity(Long turmaId, Long alunoId) {
        this.turmaId = turmaId;
        this.alunoId = alunoId;
    }
    
    public Long getTurmaId() {
        return turmaId;
    }
    
    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }
    
    public Long getAlunoId() {
        return alunoId;
    }
    
    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }
    
    public TurmaEntity getTurma() {
        return turma;
    }
    
    public void setTurma(TurmaEntity turma) {
        this.turma = turma;
    }
    
    public UsuarioEntity getAluno() {
        return aluno;
    }
    
    public void setAluno(UsuarioEntity aluno) {
        this.aluno = aluno;
    }
}
