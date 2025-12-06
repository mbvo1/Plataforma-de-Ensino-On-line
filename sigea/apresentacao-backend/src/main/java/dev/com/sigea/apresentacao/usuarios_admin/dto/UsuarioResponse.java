package dev.com.sigea.apresentacao.usuarios_admin.dto;
import lombok.Data;

@Data
public class UsuarioResponse {
    private String id;
    private String nome;
    private String email;
    private String perfil;
    private String status;
    private String senhaProvisoria;
}
