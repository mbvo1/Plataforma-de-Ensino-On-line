package dev.com.sigea.dominio.compartilhado.padroes.iterator;

import java.util.Iterator;

/**
 * Interface para agregados que podem ser iterados.
 */
public interface IteradorAgregado<T> {
    Iterator<T> criarIterator();
    int tamanho();
}

