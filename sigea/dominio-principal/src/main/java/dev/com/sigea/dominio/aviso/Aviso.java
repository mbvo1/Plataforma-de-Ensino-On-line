package dev.com.sigea.dominio.aviso;

import dev.com.sigea.dominio.turma.TurmaId;
import dev.com.sigea.dominio.usuario.UsuarioId;
import java.time.LocalDateTime;
import java.util.Objects;

public class Aviso {
    private final AvisoId id;
    private final String titulo;
    private final String conteudo;
    private final UsuarioId autorId;
    private final TurmaId turmaId; 
    private final LocalDateTime dataPublicacao;

    public Aviso(AvisoId id, UsuarioId autorId, TurmaId turmaId, String titulo, String conteudo) {
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("Título é obrigatório.");
        this.id = Objects.requireNonNull(id);
        this.autorId = Objects.requireNonNull(autorId);
        this.turmaId = Objects.requireNonNull(turmaId);
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.dataPublicacao = LocalDateTime.now();
    }

    public AvisoId getId() { return id; }
    public String getTitulo() { return titulo; }
    public TurmaId getTurmaId() { return turmaId; }
}