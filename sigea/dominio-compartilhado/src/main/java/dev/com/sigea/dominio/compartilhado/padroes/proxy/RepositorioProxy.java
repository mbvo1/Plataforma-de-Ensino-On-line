package dev.com.sigea.dominio.compartilhado.padroes.proxy;

import java.util.Optional;

/**
 * Interface Proxy para repositórios com cache.
 */
public interface RepositorioProxy<T, ID> {
    void salvar(T entidade);
    Optional<T> buscarPorId(ID id);
    void limparCache();
}

