package dev.com.sigea.dominio.disciplina;
import java.util.Objects;
public class Disciplina {
    private DisciplinaId id;
    private String nome;

    public Disciplina(DisciplinaId id, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da disciplina é obrigatório.");
        }
        this.id = Objects.requireNonNull(id);
        this.nome = nome;
    }

    public DisciplinaId getId() { return id; }
    public String getNome() { return nome; }
}