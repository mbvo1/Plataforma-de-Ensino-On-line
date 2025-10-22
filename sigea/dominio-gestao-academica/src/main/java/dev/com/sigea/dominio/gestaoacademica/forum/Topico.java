package dev.com.sigea.dominio.gestaoacademica.forum;

import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaId;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import java.util.Objects;

public class Topico {
    private final TopicoId id;
    private final DisciplinaId disciplinaId;
    private final String titulo;
    private final String conteudo;
    private final UsuarioId autorId;

    public Topico(TopicoId id, DisciplinaId disciplinaId, String titulo, String conteudo, UsuarioId autorId) {
        this.id = Objects.requireNonNull(id);
        this.disciplinaId = Objects.requireNonNull(disciplinaId);
        this.titulo = Objects.requireNonNull(titulo);
        this.conteudo = Objects.requireNonNull(conteudo);
        this.autorId = Objects.requireNonNull(autorId);
    }

    public TopicoId getId() { return id; }
    public DisciplinaId getDisciplinaId() { return disciplinaId; }
    public String getTitulo() { return titulo; }
    public UsuarioId getAutorId() { return autorId; }
}