package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AtividadeResponse {
    private String id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataLimite;
    private boolean temPrazo;
    private int totalEnvios;
}
