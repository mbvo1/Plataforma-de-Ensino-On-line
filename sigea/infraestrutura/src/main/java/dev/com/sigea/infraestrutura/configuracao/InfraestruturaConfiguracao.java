package dev.com.sigea.infraestrutura.configuracao;

import dev.com.sigea.dominio.compartilhado.EventoBarramento;
import dev.com.sigea.infraestrutura.eventos.EventoBarramentoImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraestruturaConfiguracao {
    
    @Bean
    public EventoBarramento eventoBarramento(ApplicationEventPublisher publisher) {
        return new EventoBarramentoImpl(publisher);
    }
}

