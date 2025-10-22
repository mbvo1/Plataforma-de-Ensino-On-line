package dev.com.sigea.dominio.comunicacao.aviso;

import dev.com.sigea.dominio.ambientecolaborativo.turma.Turma;
import dev.com.sigea.dominio.ambientecolaborativo.turma.TurmaId;
import dev.com.sigea.dominio.ambientecolaborativo.turma.TurmaRepository;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

public class PublicadorDeAvisoService {
    private final AvisoRepository avisoRepository;
    private final TurmaRepository turmaRepository;

    public PublicadorDeAvisoService(AvisoRepository avisoRepository, TurmaRepository turmaRepository) {
        this.avisoRepository = avisoRepository;
        this.turmaRepository = turmaRepository;
    }

    public Aviso publicarParaTurma(UsuarioId professorId, TurmaId turmaId, String titulo, String conteudo) {
        Turma turma = turmaRepository.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));

        if (!turma.getProfessorCriadorId().equals(professorId)) {
            throw new SecurityException("O usuário não tem permissão para publicar avisos nesta turma.");
        }

        Aviso novoAviso = new Aviso(avisoRepository.proximoId(), professorId, turmaId, titulo, conteudo);
        avisoRepository.salvar(novoAviso);
        return novoAviso;
    }
}