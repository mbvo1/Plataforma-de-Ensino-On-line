package dev.com.sigea.dominio.usuario;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarNovoUsuario(String nome, String email, String senhaTexto, Perfil perfil) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso");
        }

        UsuarioId novoId = usuarioRepository.proximoId();
        Senha senha = new Senha("HASH_" + senhaTexto); 
        Usuario novoUsuario = new Usuario(novoId, nome, email, senha, perfil);
        usuarioRepository.salvar(novoUsuario);

        return novoUsuario;
    }
}