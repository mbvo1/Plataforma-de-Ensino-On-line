package dev.com.sigea.apresentacao.conteudo_didatico.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Atividade com prazo obrigatório
 */
public class DeadlineObrigatorioStrategy implements ValidacaoDeadlineStrategy {
    
    @Override
    public boolean validar(LocalDateTime dataLimite) {
        if (dataLimite == null) {
            throw new IllegalArgumentException("Data limite é obrigatória para este tipo de atividade");
        }
        
        if (dataLimite.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data limite não pode estar no passado");
        }
        
        return true;
    }
    
    @Override
    public boolean isPrazoObrigatorio() {
        return true;
    }
}
