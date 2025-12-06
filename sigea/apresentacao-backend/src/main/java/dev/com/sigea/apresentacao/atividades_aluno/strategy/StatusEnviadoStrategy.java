package dev.com.sigea.apresentacao.atividades_aluno.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Atividade enviada no prazo
 */
public class StatusEnviadoStrategy implements StatusEnvioStrategy {
    
    @Override
    public String determinarStatus(LocalDateTime dataEnvio, LocalDateTime prazo, boolean corrigido) {
        if (dataEnvio == null) {
            return null; // Não foi enviada
        }
        
        if (corrigido) {
            return "CORRIGIDO";
        }
        
        if (prazo != null && dataEnvio.isAfter(prazo)) {
            return "ATRASADO";
        }
        
        return "ENVIADO";
    }
}
