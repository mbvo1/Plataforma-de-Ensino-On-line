package dev.com.sigea.apresentacao.conteudo_didatico.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Atividade com prazo opcional (estudo contínuo)
 */
public class DeadlineOpcionalStrategy implements ValidacaoDeadlineStrategy {
    
    @Override
    public boolean validar(LocalDateTime dataLimite) {
        // Prazo é opcional, mas se fornecido deve ser no futuro
        if (dataLimite != null && dataLimite.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data limite não pode estar no passado");
        }
        
        return true;
    }
    
    @Override
    public boolean isPrazoObrigatorio() {
        return false;
    }
}
