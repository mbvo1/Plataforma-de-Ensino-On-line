package dev.com.sigea.apresentacao.desempenho_academico.dto;

import lombok.Data;

@Data
public class RegistrarFrequenciaRequest {
    private String alunoId;
    private int faltas;
}
