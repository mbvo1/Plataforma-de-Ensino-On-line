package dev.com.sigea.dominio.sala;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Optional;

public class Sala {

    private final SalaId id;
    private final DisciplinaId disciplinaId;
    private final int limiteDeVagas;
    private final List<Matricula> matriculas = new ArrayList<>();

    public Sala(SalaId id, DisciplinaId disciplinaId, int limiteDeVagas) {
        this.id = Objects.requireNonNull(id);
        this.disciplinaId = Objects.requireNonNull(disciplinaId);
        this.limiteDeVagas = limiteDeVagas;
    }

    public boolean isAlunoMatriculado(UsuarioId alunoId) {
        return matriculas.stream()
                .anyMatch(m -> m.getAlunoId().equals(alunoId));
    }
    
    public Matricula matricular(UsuarioId alunoId) {
        if (getVagasRestantes() <= 0) {
            throw new IllegalStateException("Não há vagas disponíveis para esta sala.");
        }
        if (matriculas.stream().anyMatch(m -> m.getAlunoId().equals(alunoId))) {
            throw new IllegalStateException("Aluno já matriculado nesta sala.");
        }
        Matricula novaMatricula = new Matricula(new MatriculaId(UUID.randomUUID().toString()), alunoId);
        this.matriculas.add(novaMatricula);
        return novaMatricula;
    }

    public void lancarNota(UsuarioId alunoId, String avaliacao, Nota nota) {
        Matricula matricula = buscarMatriculaDoAluno(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado nesta sala."));
        
        matricula.registrarNota(avaliacao, nota);
    }

    private Optional<Matricula> buscarMatriculaDoAluno(UsuarioId alunoId) {
        return matriculas.stream()
                .filter(m -> m.getAlunoId().equals(alunoId))
                .findFirst();
    }

    public SalaId getId() {
        return id;
    }

    public DisciplinaId getDisciplinaId() {
        return disciplinaId;
    }

    public List<Matricula> getMatriculas() {
        return List.copyOf(matriculas);
    }

    public int getVagasRestantes() {
        return limiteDeVagas - matriculas.size();
    }
}