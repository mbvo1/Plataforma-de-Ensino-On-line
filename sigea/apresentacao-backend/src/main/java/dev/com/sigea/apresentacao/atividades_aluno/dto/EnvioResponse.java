package dev.com.sigea.apresentacao.atividades_aluno.dto;

import lombok.Data;

@Data
public class EnvioResponse {
    private String id;
    private String atividadeId;
    private String titulo;
    private String status; // PENDENTE, ENVIADO, ATRASADO, CORRIGIDO
    private String nota;
    private String dataEnvio;
    private String prazo;
}
