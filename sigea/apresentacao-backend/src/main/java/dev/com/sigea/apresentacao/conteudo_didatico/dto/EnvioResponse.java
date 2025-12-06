package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnvioResponse {
    private String id;
    private String alunoId;
    private String nomeAluno;
    private LocalDateTime dataEnvio;
    private String status;
    private Double nota;
    private String feedback;
}
