package dev.com.sigea.apresentacao.atividades_aluno.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnviarAtividadeRequest {
    private String atividadeId;
    private String alunoId;
    private List<String> arquivos;
    private String observacoes;
}
