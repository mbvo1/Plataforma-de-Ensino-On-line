package dev.com.sigea.dominio.compartilhado;

public interface EventoObservador {
    void processar(EventoDominio evento);
}

