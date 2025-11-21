package dev.com.sigea.aplicacao.autenticacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long usuarioId;
    private String nome;
    private String email;
    private String perfil;
    private String token; 
}

