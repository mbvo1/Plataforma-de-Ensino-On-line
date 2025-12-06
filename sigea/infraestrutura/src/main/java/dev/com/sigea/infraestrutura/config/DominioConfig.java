package dev.com.sigea.infraestrutura.config;

import dev.com.sigea.dominio.turma.TurmaRepository;
import dev.com.sigea.dominio.turma.TurmaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DominioConfig {
    
    @Bean
    public TurmaService turmaService(TurmaRepository turmaRepository) {
        return new TurmaService(turmaRepository);
    }
}
