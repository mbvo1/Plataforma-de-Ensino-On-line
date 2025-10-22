package dev.com.sigea.dominio.ambientecolaborativo.turma;

import java.util.List;
import java.util.Objects;

public class Material {
    private final MaterialId id;
    private final String titulo;
    private final String descricao;
    private final List<Anexo> anexos;

    Material(MaterialId id, String titulo, String descricao, List<Anexo> anexos) {
        this.id = Objects.requireNonNull(id);
        this.titulo = Objects.requireNonNull(titulo);
        this.descricao = Objects.requireNonNull(descricao);
        this.anexos = Objects.requireNonNull(anexos);
    }

    public MaterialId getId() { return id; }
    public String getTitulo() { return titulo; }
    public List<Anexo> getAnexos() { return List.copyOf(anexos); }
}