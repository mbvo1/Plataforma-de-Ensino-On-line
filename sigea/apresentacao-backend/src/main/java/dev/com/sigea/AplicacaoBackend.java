package dev.com.sigea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"dev.com.sigea"})
@EnableJpaRepositories(basePackages = "dev.com.sigea.infraestrutura.persistencia")
@EntityScan(basePackages = "dev.com.sigea.infraestrutura.persistencia")
public class AplicacaoBackend {
    public static void main(String[] args) {
        SpringApplication.run(AplicacaoBackend.class, args);
    }
}