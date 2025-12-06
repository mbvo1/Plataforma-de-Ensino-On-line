package dev.com.sigea.apresentacao.disciplinas_periodos.dto;
import lombok.Data;

@Data
public class PeriodoResponse {
    private String id;
    private String nome;
    private String dataInicio;
    private String dataFim;
    private boolean ativo;
}
