package dev.com.sigea.apresentacao.conteudo_didatico.strategy;

import dev.com.sigea.dominio.turma.TurmaId;
import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Strategy Pattern - Define estratégias de validação de escopo para publicação de conteúdo
 */
public interface ValidacaoEscopoStrategy {
    
    /**
     * Valida se o professor tem permissão para publicar na turma
     * 
     * @param professorId ID do professor
     * @param turmaId ID da turma
     * @return true se válido, lança exceção se inválido
     */
    boolean validar(UsuarioId professorId, TurmaId turmaId);
}
