package dev.com.sigea.apresentacao.desempenho_academico.dto;

import lombok.Data;

@Data
public class LancarNotaRequest {
    private String alunoId;
    private String avaliacao;
    private Double nota;
}
