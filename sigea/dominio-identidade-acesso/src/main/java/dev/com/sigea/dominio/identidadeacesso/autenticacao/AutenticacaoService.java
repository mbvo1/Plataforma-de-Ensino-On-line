package dev.com.sigea.dominio.identidadeacesso.autenticacao;

import dev.com.sigea.dominio.identidadeacesso.usuario.Usuario;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioRepository;

import java.util.Objects;

public class AutenticacaoService {
    
    private final UsuarioRepository usuarioRepository;
    
    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }
    
    public Usuario autenticar(Credencial credencial) {
        Objects.requireNonNull(credencial, "Credenciais não podem ser nulas.");
        
        Usuario usuario = usuarioRepository.buscarPorEmail(credencial.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos."));
        
        if (usuario.getStatus() != dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioStatus.ATIVO) {
            throw new IllegalStateException("Usuário inativo. Entre em contato com o administrador.");
        }
        
        if (!usuario.verificarSenha(credencial.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos.");
        }
        
        return usuario;
    }
}

