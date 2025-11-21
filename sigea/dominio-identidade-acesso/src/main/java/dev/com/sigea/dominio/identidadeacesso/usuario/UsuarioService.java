package dev.com.sigea.dominio.identidadeacesso.usuario;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.Senha;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarNovoUsuario(String nome, String email, Perfil perfil) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso");
        }

        UsuarioId novoId = usuarioRepository.proximoId();
        Usuario novoUsuario = new Usuario(novoId, nome, email, perfil);
        usuarioRepository.salvar(novoUsuario);

        return novoUsuario;
    }
    
    public Usuario cadastrarNovoUsuario(String nome, String email, Senha senha, Perfil perfil) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso");
        }

        UsuarioId novoId = usuarioRepository.proximoId();
        Usuario novoUsuario = new Usuario(novoId, nome, email, senha, perfil);
        usuarioRepository.salvar(novoUsuario);

        return novoUsuario;
    }
}
