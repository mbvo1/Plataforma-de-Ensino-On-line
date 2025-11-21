package dev.com.sigea.dominio.compartilhado;

import java.time.LocalDateTime;

public interface EventoDominio {
    
    String tipo();
    LocalDateTime ocorridoEm();
}

