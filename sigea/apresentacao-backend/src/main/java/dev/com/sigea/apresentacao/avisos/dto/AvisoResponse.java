package dev.com.sigea.apresentacao.avisos.dto;

import lombok.Data;

@Data
public class AvisoResponse {
    private String id;
    private String titulo;
    private String conteudo;
    private String autorId;
    private String disciplinaId;
    private String prioridade;
    private String escopo;
    private String dataExpiracao;
    private boolean lido;
}
