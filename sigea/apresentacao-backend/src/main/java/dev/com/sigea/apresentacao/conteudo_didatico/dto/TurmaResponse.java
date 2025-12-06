package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;

@Data
public class TurmaResponse {
    private String id;
    private String titulo;
    private String codigoAcesso;
    private String professorCriadorId;
}
