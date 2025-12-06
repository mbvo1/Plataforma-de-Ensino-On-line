package dev.com.sigea.apresentacao.conteudo_didatico.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Define estratégias de validação de deadline (prazo)
 */
public interface ValidacaoDeadlineStrategy {
    
    /**
     * Valida o prazo da atividade
     * 
     * @param dataLimite Data limite (pode ser null)
     * @return true se válido
     */
    boolean validar(LocalDateTime dataLimite);
    
    /**
     * Verifica se a atividade tem prazo obrigatório
     */
    boolean isPrazoObrigatorio();
}
