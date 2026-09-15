package dev.com.sigea.dominio.usuario;

public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(String nome, String email, String cpf, String senhaTexto, Perfil perfil) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        UsuarioId novoId = usuarioRepository.proximoId();
        Senha senha = Senha.criarNova(senhaTexto);
        Usuario usuario = new Usuario(novoId, nome, email, cpf, senha, perfil);

        usuarioRepository.salvar(usuario);

        return usuario;
    }

    public Usuario autenticar(String email, String senhaTexto) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!usuario.getSenha().verificar(senhaTexto)) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        if (usuario.getStatus() == UsuarioStatus.INATIVO) {
            throw new IllegalStateException("Usuário está inativo");
        }

        return usuario;
    }
}