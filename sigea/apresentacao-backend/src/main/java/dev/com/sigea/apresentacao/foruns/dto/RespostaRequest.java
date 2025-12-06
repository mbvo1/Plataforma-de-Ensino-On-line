package dev.com.sigea.apresentacao.foruns.dto;

import dev.com.sigea.dominio.usuario.UsuarioId;
import lombok.Data;

@Data
public class RespostaRequest {
    private String conteudo;
    private String autorId;
    
    public UsuarioId getAutorId() {
        return new UsuarioId(autorId);
    }
}
