package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;

@Data
public class CorrigirAtividadeRequest {
    private Double nota;
    private String feedback;
}
