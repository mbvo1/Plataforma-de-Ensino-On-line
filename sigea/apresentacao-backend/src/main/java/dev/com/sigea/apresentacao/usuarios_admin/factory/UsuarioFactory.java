package dev.com.sigea.apresentacao.usuarios_admin.factory;

import dev.com.sigea.apresentacao.usuarios_admin.dto.UsuarioResponse;
import java.util.UUID;

/**
 * Factory Pattern - Cria usuários com senha provisória
 */
public class UsuarioFactory {
    
    public static UsuarioResponse criarProfessor(String nome, String email, String especialidade) {
        UsuarioResponse usuario = new UsuarioResponse();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfil("PROFESSOR");
        usuario.setStatus("ATIVO");
        usuario.setSenhaProvisoria(gerarSenhaProvisoria());
        
        System.out.println("👨‍🏫 Professor criado: " + nome + " | Especialidade: " + especialidade);
        
        return usuario;
    }
    
    public static UsuarioResponse criarAluno(String nome, String email) {
        UsuarioResponse usuario = new UsuarioResponse();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfil("ALUNO");
        usuario.setStatus("ATIVO");
        usuario.setSenhaProvisoria(gerarSenhaProvisoria());
        
        System.out.println("👨‍🎓 Aluno criado: " + nome);
        
        return usuario;
    }
    
    private static String gerarSenhaProvisoria() {
        return "Temp" + UUID.randomUUID().toString().substring(0, 8);
    }
}
