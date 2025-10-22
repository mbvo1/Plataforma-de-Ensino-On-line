package dev.com.sigea.dominio.identidadeacesso.usuario;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarNovoUsuario(String nome, String email, Perfil perfil) {
        // Verificar se o e-mail já existe
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso");
        }

        // Criar novo usuário
        UsuarioId novoId = usuarioRepository.proximoId();
        Usuario novoUsuario = new Usuario(novoId, nome, email, perfil);

        // Salvar o usuário
        usuarioRepository.salvar(novoUsuario);

        return novoUsuario;
    }
}
