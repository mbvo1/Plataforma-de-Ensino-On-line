package dev.com.sigea.dominio.usuario;

import java.util.Optional;

public interface UsuarioRepository {

    void salvar(Usuario usuario);
    
    Optional<Usuario> buscarPorEmail(String email);

    UsuarioId proximoId();
}

