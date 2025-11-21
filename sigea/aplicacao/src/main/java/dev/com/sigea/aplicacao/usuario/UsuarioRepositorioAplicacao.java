package dev.com.sigea.aplicacao.usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorioAplicacao {
    
    Optional<UsuarioResumo> buscarResumoPorId(Long id);
    
    Optional<UsuarioResumo> buscarResumoPorEmail(String email);
    
    List<UsuarioResumo> listarResumos();
    
    List<UsuarioResumo> buscarPorPerfil(String perfil);
}

