package dev.com.sigea.dominio.aviso;
import dev.com.sigea.dominio.usuario.*;
import java.util.*;
public class UsuarioRepositoryEmMemoria implements UsuarioRepository {
    private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
    @Override public void salvar(Usuario usuario) { usuariosPorEmail.put(usuario.getEmail(), usuario); }
    @Override public Optional<Usuario> buscarPorEmail(String email) { return Optional.ofNullable(usuariosPorEmail.get(email)); }
    @Override public UsuarioId proximoId() { return new UsuarioId(String.valueOf(usuariosPorEmail.size() + 1)); }
}

