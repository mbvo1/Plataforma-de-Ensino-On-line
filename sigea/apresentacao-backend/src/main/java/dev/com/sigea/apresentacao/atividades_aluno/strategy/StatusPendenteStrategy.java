package dev.com.sigea.apresentacao.atividades_aluno.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Atividade pendente (não enviada)
 */
public class StatusPendenteStrategy implements StatusEnvioStrategy {
    
    @Override
    public String determinarStatus(LocalDateTime dataEnvio, LocalDateTime prazo, boolean corrigido) {
        if (dataEnvio != null) {
            return null; // Não é mais pendente
        }
        
        if (prazo != null && LocalDateTime.now().isAfter(prazo)) {
            return "ATRASADO";
        }
        
        return "PENDENTE";
    }
}
