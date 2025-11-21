package dev.com.sigea.dominio.compartilhado;

import java.time.LocalDateTime;

public record MatriculaRealizadaEvento(
    Long alunoId,
    Long salaId,
    LocalDateTime ocorridoEm
) implements EventoDominio {
    
    public MatriculaRealizadaEvento(Long alunoId, Long salaId) {
        this(alunoId, salaId, LocalDateTime.now());
    }
    
    @Override
    public String tipo() {
        return "MatriculaRealizada";
    }
}

