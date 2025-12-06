package dev.com.sigea.apresentacao.conteudo_didatico.strategy;

import dev.com.sigea.dominio.turma.Turma;
import dev.com.sigea.dominio.turma.TurmaId;
import dev.com.sigea.dominio.turma.TurmaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Strategy Pattern - Valida se o professor é o criador da turma
 */
public class ValidacaoTurmaProfessorStrategy implements ValidacaoEscopoStrategy {
    
    private final TurmaRepository turmaRepository;
    
    public ValidacaoTurmaProfessorStrategy(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }
    
    @Override
    public boolean validar(UsuarioId professorId, TurmaId turmaId) {
        Turma turma = turmaRepository.buscarPorId(turmaId)
            .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        
        if (!turma.getProfessorCriadorId().equals(professorId)) {
            throw new SecurityException("Apenas o professor criador da turma pode publicar materiais.");
        }
        
        return true;
    }
}
