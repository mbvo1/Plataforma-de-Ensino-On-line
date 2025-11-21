package dev.com.sigea.aplicacao.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumo {
    private Long id;
    private String nome;
    private String email;
    private String perfil;
    private String status;
}

