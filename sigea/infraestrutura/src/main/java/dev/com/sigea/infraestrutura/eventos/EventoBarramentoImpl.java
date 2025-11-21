package dev.com.sigea.infraestrutura.eventos;

import dev.com.sigea.dominio.compartilhado.EventoBarramento;
import dev.com.sigea.dominio.compartilhado.EventoDominio;
import dev.com.sigea.dominio.compartilhado.EventoObservador;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventoBarramentoImpl implements EventoBarramento {
    
    private final ApplicationEventPublisher publisher;
    private final List<EventoObservador> observadores;
    
    public EventoBarramentoImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.observadores = new ArrayList<>();
    }
    
    @Override
    public void publicar(EventoDominio evento) {
        publisher.publishEvent(evento);
        
        observadores.forEach(obs -> obs.processar(evento));
    }
    
    @Override
    public void registrar(EventoObservador observador) {
        observadores.add(observador);
    }
}

