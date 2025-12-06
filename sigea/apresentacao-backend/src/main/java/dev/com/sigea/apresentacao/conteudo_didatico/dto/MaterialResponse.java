package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;
import java.util.List;

@Data
public class MaterialResponse {
    private String id;
    private String titulo;
    private String descricao;
    private List<AnexoResponse> anexos;
    private String versao;
    private boolean temPrazo;
}
