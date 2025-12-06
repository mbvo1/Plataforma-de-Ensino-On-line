package dev.com.sigea.apresentacao.desempenho_academico.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DesempenhoResponse {
    private String disciplina;
    private Map<String, Double> notas;
    private int faltas;
    private String situacaoFinal;
}
