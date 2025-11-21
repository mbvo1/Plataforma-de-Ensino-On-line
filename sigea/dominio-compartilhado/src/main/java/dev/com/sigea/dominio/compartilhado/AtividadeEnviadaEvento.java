package dev.com.sigea.dominio.compartilhado;

import java.time.LocalDateTime;

public record AtividadeEnviadaEvento(
    Long alunoId,
    Long atividadeId,
    Long turmaId,
    LocalDateTime ocorridoEm
) implements EventoDominio {
    
    public AtividadeEnviadaEvento(Long alunoId, Long atividadeId, Long turmaId) {
        this(alunoId, atividadeId, turmaId, LocalDateTime.now());
    }
    
    @Override
    public String tipo() {
        return "AtividadeEnviada";
    }
}

