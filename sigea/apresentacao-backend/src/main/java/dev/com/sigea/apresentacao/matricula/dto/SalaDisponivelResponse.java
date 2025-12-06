package dev.com.sigea.apresentacao.matricula.dto;
import lombok.Data;

@Data
public class SalaDisponivelResponse {
    private String id;
    private String disciplinaNome;
    private String professorNome;
    private String horario;
    private int vagasDisponiveis;
    private int vagasTotal;
}
