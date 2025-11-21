package dev.com.sigea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Classe principal da aplicação Spring Boot.
 * Configura o escaneamento de componentes, entidades JPA e repositórios.
 */
@SpringBootApplication(scanBasePackages = {
    "dev.com.sigea.apresentacao",
    "dev.com.sigea.aplicacao",
    "dev.com.sigea.infraestrutura"
})
@EntityScan(basePackages = "dev.com.sigea.infraestrutura.persistencia.jpa")
@EnableJpaRepositories(basePackages = "dev.com.sigea.infraestrutura.persistencia.jpa")
public class AplicacaoBackend {
    public static void main(String[] args) {
        SpringApplication.run(AplicacaoBackend.class, args);
    }
}

