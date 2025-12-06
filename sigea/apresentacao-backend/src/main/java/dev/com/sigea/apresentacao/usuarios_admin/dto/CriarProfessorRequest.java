package dev.com.sigea.apresentacao.usuarios_admin.dto;
import lombok.Data;

@Data
public class CriarProfessorRequest {
    private String nome;
    private String email;
    private String especialidade;
}
