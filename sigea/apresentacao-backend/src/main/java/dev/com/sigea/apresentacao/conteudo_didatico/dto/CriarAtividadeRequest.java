package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CriarAtividadeRequest {
    private String titulo;
    private String descricao;
    private LocalDateTime dataLimite; // Opcional
}
