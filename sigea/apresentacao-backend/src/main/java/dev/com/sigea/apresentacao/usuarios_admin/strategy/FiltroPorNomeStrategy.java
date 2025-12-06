package dev.com.sigea.apresentacao.usuarios_admin.strategy;

import dev.com.sigea.apresentacao.usuarios_admin.dto.UsuarioResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern - Filtra por nome
 */
public class FiltroPorNomeStrategy implements FiltroUsuarioStrategy {
    
    @Override
    public List<UsuarioResponse> filtrar(List<UsuarioResponse> usuarios, String criterio) {
        return usuarios.stream()
                .filter(u -> u.getNome().toLowerCase().contains(criterio.toLowerCase()))
                .collect(Collectors.toList());
    }
}
