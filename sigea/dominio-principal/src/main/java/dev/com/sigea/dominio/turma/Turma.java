package dev.com.sigea.dominio.turma;

import dev.com.sigea.dominio.usuario.UsuarioId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Turma {

    private final TurmaId id;
    private final String titulo;
    private final CodigoAcesso codigoAcesso;
    private final UsuarioId professorCriadorId;
    private final List<UsuarioId> alunosParticipantes = new ArrayList<>();
    private final List<Material> materiais = new ArrayList<>();
    private final List<Atividade> atividades = new ArrayList<>();

    public Turma(TurmaId id, UsuarioId professorCriadorId, String titulo) {
        this.id = Objects.requireNonNull(id);
        this.professorCriadorId = Objects.requireNonNull(professorCriadorId);
        this.titulo = Objects.requireNonNull(titulo);
        this.codigoAcesso = new CodigoAcesso(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public Material publicarMaterial(UsuarioId professorId, String titulo, String descricao, List<Anexo> anexos) {
        if (!Objects.equals(professorId, this.professorCriadorId)) {
            throw new SecurityException("Apenas o professor criador da turma pode publicar materiais.");
        }

        var novoId = new MaterialId(UUID.randomUUID().toString());
        var novoMaterial = new Material(novoId, titulo, descricao, anexos);
        this.materiais.add(novoMaterial);
        return novoMaterial;
    }

    public TurmaId getId() { return id; }
    public String getTitulo() { return titulo; }
    public CodigoAcesso getCodigoAcesso() { return codigoAcesso; }
    public UsuarioId getProfessorCriadorId() { return professorCriadorId; }
    public List<UsuarioId> getAlunosParticipantes() { return List.copyOf(alunosParticipantes); }

    public List<Material> getMateriais() {
        return List.copyOf(materiais);
    }

    public void ingressar(UsuarioId alunoId) {
        Objects.requireNonNull(alunoId);
        if (alunosParticipantes.contains(alunoId)) {
            throw new IllegalArgumentException("Aluno já está participando desta turma.");
        }
        alunosParticipantes.add(alunoId);
    }

    public Envio enviarAtividade(AtividadeId atividadeId, UsuarioId alunoId, List<Anexo> anexos) {
        Objects.requireNonNull(atividadeId);
        Objects.requireNonNull(alunoId);
        Objects.requireNonNull(anexos);

        if (!alunosParticipantes.contains(alunoId)) {
            throw new IllegalArgumentException("Aluno não está participando desta turma.");
        }

        var atividade = atividades.stream()
            .filter(a -> a.getId().equals(atividadeId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada nesta turma."));

        return atividade.receberEnvio(alunoId, anexos);
    }

    public List<Atividade> getAtividades() {
        return List.copyOf(atividades);
    }

    public void adicionarAtividade(Atividade atividade) {
        Objects.requireNonNull(atividade);
        this.atividades.add(atividade);
    }
}