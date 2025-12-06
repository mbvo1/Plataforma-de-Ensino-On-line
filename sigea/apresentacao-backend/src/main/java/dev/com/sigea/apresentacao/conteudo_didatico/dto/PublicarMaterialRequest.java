package dev.com.sigea.apresentacao.conteudo_didatico.dto;

import lombok.Data;
import java.util.List;

@Data
public class PublicarMaterialRequest {
    private String titulo;
    private String descricao;
    private List<AnexoRequest> anexos;
}
