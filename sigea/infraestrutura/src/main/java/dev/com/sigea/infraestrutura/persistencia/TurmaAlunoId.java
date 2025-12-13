package dev.com.sigea.infraestrutura.persistencia;

import java.io.Serializable;
import java.util.Objects;

public class TurmaAlunoId implements Serializable {
    private Long turmaId;
    private Long alunoId;
    
    public TurmaAlunoId() {
    }
    
    public TurmaAlunoId(Long turmaId, Long alunoId) {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurmaAlunoId that = (TurmaAlunoId) o;
        return Objects.equals(turmaId, that.turmaId) && Objects.equals(alunoId, that.alunoId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(turmaId, alunoId);
    }
}
