package dev.com.sigea.dominio.compartilhado;

public interface EventoBarramento {
    void publicar(EventoDominio evento);
    
    void registrar(EventoObservador observador);
}

