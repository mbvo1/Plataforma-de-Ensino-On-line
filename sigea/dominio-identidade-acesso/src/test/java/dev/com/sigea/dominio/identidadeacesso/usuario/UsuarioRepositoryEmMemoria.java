package dev.com.sigea.dominio.identidadeacesso.usuario; 

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// Implementação do Adaptador de persistência para testes em memória
public class UsuarioRepositoryEmMemoria implements UsuarioRepository {

    private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(Usuario usuario) {
        if (buscarPorEmail(usuario.getEmail()).isPresent() && 
            !buscarPorEmail(usuario.getEmail()).get().getId().equals(usuario.getId())) {
            throw new IllegalStateException("Este e-mail já está em uso");
        }
        usuariosPorEmail.put(usuario.getEmail(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return Optional.ofNullable(usuariosPorEmail.get(email));
    }
    
    @Override
    public UsuarioId proximoId() {
        return new UsuarioId(sequence.getAndIncrement());
    }
    
    public int totalDeUsuarios() {
        return usuariosPorEmail.size();
    }
}