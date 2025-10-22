package dev.com.sigea.dominio.ambientecolaborativo.turma;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

import java.util.List;

public class TurmaService {

    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    public Material publicarMaterialNaTurma(UsuarioId professorId, TurmaId turmaId, String titulo, String descricao, List<Anexo> anexos) {
        // Buscar a turma
        Turma turma = turmaRepository.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));

        // Publicar o material (a lógica de permissão está na entidade Turma)
        Material material = turma.publicarMaterial(professorId, titulo, descricao, anexos);

        // Salvar o estado da turma
        turmaRepository.salvar(turma);

        return material;
    }

    public Atividade publicarAtividadeNaTurma(UsuarioId professorId, TurmaId turmaId, String titulo, String descricao, java.time.LocalDateTime dataLimite) {
        // Buscar a turma
        Turma turma = turmaRepository.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));

        // Verificar permissão (apenas o professor criador pode publicar atividades)
        if (!turma.getProfessorCriadorId().equals(professorId)) {
            throw new SecurityException("Apenas o professor criador da turma pode publicar atividades.");
        }

        // Criar e adicionar a atividade
        AtividadeId novoId = new AtividadeId(java.util.UUID.randomUUID().toString());
        Atividade novaAtividade = new Atividade(novoId, titulo, descricao, dataLimite);
        turma.adicionarAtividade(novaAtividade);

        // Salvar o estado da turma
        turmaRepository.salvar(turma);

        return novaAtividade;
    }
}
