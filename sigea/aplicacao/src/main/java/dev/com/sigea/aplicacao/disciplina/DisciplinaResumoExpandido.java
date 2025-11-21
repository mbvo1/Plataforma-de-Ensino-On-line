package dev.com.sigea.aplicacao.disciplina;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaResumoExpandido {
    private Long id;
    private String codigoDisciplina;
    private String nome;
    private String descricao;
    private List<Long> preRequisitosIds;
}

