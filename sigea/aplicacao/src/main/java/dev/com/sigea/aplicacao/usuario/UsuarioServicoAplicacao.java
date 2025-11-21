package dev.com.sigea.aplicacao.usuario;

import java.util.List;
import java.util.Optional;

public class UsuarioServicoAplicacao {
    
    private final UsuarioRepositorioAplicacao repositorio;
    
    public UsuarioServicoAplicacao(UsuarioRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }
    
    public Optional<UsuarioResumo> buscarPorId(Long id) {
        return repositorio.buscarResumoPorId(id);
    }
    
    public Optional<UsuarioResumo> buscarPorEmail(String email) {
        return repositorio.buscarResumoPorEmail(email);
    }
    
    public List<UsuarioResumo> listarTodos() {
        return repositorio.listarResumos();
    }
    
    public List<UsuarioResumo> buscarPorPerfil(String perfil) {
        return repositorio.buscarPorPerfil(perfil);
    }
}

