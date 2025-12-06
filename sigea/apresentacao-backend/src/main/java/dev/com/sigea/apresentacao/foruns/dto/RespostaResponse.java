package dev.com.sigea.apresentacao.foruns.dto;

import lombok.Data;

@Data
public class RespostaResponse {
    private String id;
    private String conteudo;
    private String autorId;
    private String nomeAutor;
    private boolean isProfessor;
    private boolean verificadaPeloProfessor;
}
