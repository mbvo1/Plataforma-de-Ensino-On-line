package dev.com.sigea.dominio.usuario; 

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class UsuarioRepositoryEmMemoria implements UsuarioRepository {

    private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(Usuario usuario) {
        usuariosPorEmail.put(usuario.getEmail(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return Optional.ofNullable(usuariosPorEmail.get(email));
    }
    
    @Override
    public UsuarioId proximoId() {
        return new UsuarioId(String.valueOf(sequence.getAndIncrement())); 
    }
    
    public int totalDeUsuarios() {
        return usuariosPorEmail.size();
    }
}

