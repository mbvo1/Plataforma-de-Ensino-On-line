package dev.com.sigea.apresentacao.disciplinas_periodos.dto;
import lombok.Data;

@Data
public class CriarPeriodoRequest {
    private String nome;
    private String dataInicio;
    private String dataFim;
}
