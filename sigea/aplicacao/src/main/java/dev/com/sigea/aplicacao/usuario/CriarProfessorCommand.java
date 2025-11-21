package dev.com.sigea.aplicacao.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarProfessorCommand {
    private String nome;
    private String email;
    private String senha;
}

