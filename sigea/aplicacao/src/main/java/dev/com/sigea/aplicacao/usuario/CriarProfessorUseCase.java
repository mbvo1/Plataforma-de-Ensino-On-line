package dev.com.sigea.aplicacao.usuario;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.Senha;
import dev.com.sigea.dominio.identidadeacesso.usuario.Perfil;
import dev.com.sigea.dominio.identidadeacesso.usuario.Usuario;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioService;

public class CriarProfessorUseCase {
    
    private final UsuarioService usuarioService;
    
    public CriarProfessorUseCase(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    public UsuarioResumo executar(CriarProfessorCommand command) {
        Usuario novoProfessor = usuarioService.cadastrarNovoUsuario(
            command.getNome(),
            command.getEmail(),
            Senha.criar(command.getSenha()),
            Perfil.PROFESSOR
        );
        
        return new UsuarioResumo(
            Long.parseLong(novoProfessor.getId().valor()),
            novoProfessor.getNome(),
            novoProfessor.getEmail(),
            novoProfessor.getPerfil().name(),
            novoProfessor.getStatus().name()
        );
    }
}

