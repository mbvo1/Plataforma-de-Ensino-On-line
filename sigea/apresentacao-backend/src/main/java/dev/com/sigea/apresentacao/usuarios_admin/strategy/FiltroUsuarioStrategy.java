package dev.com.sigea.apresentacao.usuarios_admin.strategy;

import dev.com.sigea.apresentacao.usuarios_admin.dto.UsuarioResponse;
import java.util.List;

/**
 * Strategy Pattern - Interface para filtros de busca
 */
public interface FiltroUsuarioStrategy {
    List<UsuarioResponse> filtrar(List<UsuarioResponse> usuarios, String criterio);
}
