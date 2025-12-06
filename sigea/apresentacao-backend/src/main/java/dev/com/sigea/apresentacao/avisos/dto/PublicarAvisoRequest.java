package dev.com.sigea.apresentacao.avisos.dto;

import lombok.Data;

@Data
public class PublicarAvisoRequest {
    private String titulo;
    private String conteudo;
    private String autorId;
    private String disciplinaId;
    private String prioridade; // "ALTA", "MEDIA", "BAIXA"
    private String escopo; // "TURMA", "DISCIPLINA", "GERAL"
    private String dataExpiracao; // opcional
}
