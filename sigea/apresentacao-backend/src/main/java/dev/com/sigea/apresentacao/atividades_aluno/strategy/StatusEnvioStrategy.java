package dev.com.sigea.apresentacao.atividades_aluno.strategy;

import java.time.LocalDateTime;

/**
 * Strategy Pattern - Interface para determinar status do envio
 */
public interface StatusEnvioStrategy {
    String determinarStatus(LocalDateTime dataEnvio, LocalDateTime prazo, boolean corrigido);
}
