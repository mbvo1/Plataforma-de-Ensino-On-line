package dev.com.sigea.aplicacao.autenticacao;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.AutenticacaoService;
import dev.com.sigea.dominio.identidadeacesso.autenticacao.Credencial;
import dev.com.sigea.dominio.identidadeacesso.usuario.Usuario;

public class LoginUseCase {
    
    private final AutenticacaoService autenticacaoService;
    
    public LoginUseCase(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }
    
    public LoginResponse executar(LoginCommand command) {
        Credencial credencial = new Credencial(command.getEmail(), command.getSenha());
        Usuario usuario = autenticacaoService.autenticar(credencial);
        
        String token = "token_" + usuario.getId().valor() + "_" + System.currentTimeMillis();
        
        return new LoginResponse(
            Long.parseLong(usuario.getId().valor()),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil().name(),
            token
        );
    }
}

