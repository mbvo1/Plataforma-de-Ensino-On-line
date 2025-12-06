package dev.com.sigea.apresentacao.atividades_aluno.dto;

import lombok.Data;

@Data
public class IngressarTurmaRequest {
    private String codigoTurma;
    private String alunoId;
}
