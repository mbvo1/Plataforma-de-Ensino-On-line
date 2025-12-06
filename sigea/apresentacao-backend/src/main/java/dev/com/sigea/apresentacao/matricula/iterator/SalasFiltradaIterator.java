package dev.com.sigea.apresentacao.matricula.iterator;

import dev.com.sigea.apresentacao.matricula.dto.SalaDisponivelResponse;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator Pattern - Itera sobre salas com filtros
 */
public class SalasFiltradaIterator implements Iterator<SalaDisponivelResponse> {
    
    private final List<SalaDisponivelResponse> salas;
    private int posicao = 0;
    
    public SalasFiltradaIterator(List<SalaDisponivelResponse> salas) {
        this.salas = salas;
    }
    
    @Override
    public boolean hasNext() {
        return posicao < salas.size();
    }
    
    @Override
    public SalaDisponivelResponse next() {
        return salas.get(posicao++);
    }
}
