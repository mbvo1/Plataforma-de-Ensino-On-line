package dev.com.sigea.apresentacao.atividades_aluno.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Atividade já corrigida
 */
public class StatusCorrigidoStrategy implements StatusEnvioStrategy {
    
    @Override
    public String determinarStatus(LocalDateTime dataEnvio, LocalDateTime prazo, boolean corrigido) {
        if (!corrigido) {
            return null; // Não está corrigido
        }
        
        return "CORRIGIDO";
    }
}
