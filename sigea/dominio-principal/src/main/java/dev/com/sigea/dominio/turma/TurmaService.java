package dev.com.sigea.dominio.turma;

import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.List;

public class TurmaService {

    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    public Material publicarMaterialNaTurma(UsuarioId professorId, TurmaId turmaId, String titulo, String descricao, List<Anexo> anexos) {
        Turma turma = turmaRepository.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
        Material material = turma.publicarMaterial(professorId, titulo, descricao, anexos);
        turmaRepository.salvar(turma);

        return material;
    }

    public Atividade publicarAtividadeNaTurma(UsuarioId professorId, TurmaId turmaId, String titulo, String descricao, java.time.LocalDateTime dataLimite) {
        Turma turma = turmaRepository.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));

        if (!turma.getProfessorCriadorId().equals(professorId)) {
            throw new SecurityException("Apenas o professor criador da turma pode publicar atividades.");
        }
        AtividadeId novoId = new AtividadeId(java.util.UUID.randomUUID().toString());
        Atividade novaAtividade = new Atividade(novoId, titulo, descricao, dataLimite);
        turma.adicionarAtividade(novaAtividade);

        turmaRepository.salvar(turma);

        return novaAtividade;
    }
}