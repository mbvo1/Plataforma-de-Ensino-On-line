package dev.com.sigea.aplicacao.autenticacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {
    private String email;
    private String senha;
}

