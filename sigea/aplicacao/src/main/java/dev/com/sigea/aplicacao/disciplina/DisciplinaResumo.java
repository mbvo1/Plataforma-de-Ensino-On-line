package dev.com.sigea.aplicacao.disciplina;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaResumo {
    private Long id;
    private String codigoDisciplina;
    private String nome;
    private String descricao;
}

