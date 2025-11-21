package dev.com.sigea.aplicacao.disciplina;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarDisciplinaCommand {
    private String codigoDisciplina;
    private String nome;
    private String descricao;
}

