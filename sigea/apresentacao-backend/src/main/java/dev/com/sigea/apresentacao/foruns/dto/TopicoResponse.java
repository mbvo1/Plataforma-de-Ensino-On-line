package dev.com.sigea.apresentacao.foruns.dto;

import lombok.Data;

@Data
public class TopicoResponse {
    private String id;
    private String titulo;
    private String conteudo;
    private String autorId;
    private String disciplinaId;
    private String nomeAutor;
    private boolean isProfessor;
    private int totalRespostas;
}
