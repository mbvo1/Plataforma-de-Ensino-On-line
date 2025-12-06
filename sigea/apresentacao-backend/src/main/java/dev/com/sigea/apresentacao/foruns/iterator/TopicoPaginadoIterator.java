package dev.com.sigea.apresentacao.foruns.iterator;

import dev.com.sigea.dominio.forum.Topico;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator Pattern - Itera sobre tópicos com paginação
 */
public class TopicoPaginadoIterator implements Iterator<Topico> {
    
    private final List<Topico> topicos;
    private final int tamanhoPagina;
    private int posicaoAtual = 0;
    
    public TopicoPaginadoIterator(List<Topico> topicos, int tamanhoPagina) {
        this.topicos = topicos;
        this.tamanhoPagina = tamanhoPagina;
    }
    
    @Override
    public boolean hasNext() {
        return posicaoAtual < topicos.size();
    }
    
    @Override
    public Topico next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Não há mais tópicos");
        }
        return topicos.get(posicaoAtual++);
    }
    
    /**
     * Verifica se há próxima página
     */
    public boolean hasNextPage() {
        return posicaoAtual < topicos.size();
    }
    
    /**
     * Retorna a próxima página de tópicos
     */
    public List<Topico> nextPage() {
        int inicio = posicaoAtual;
        int fim = Math.min(inicio + tamanhoPagina, topicos.size());
        
        if (inicio >= topicos.size()) {
            throw new NoSuchElementException("Não há mais páginas");
        }
        
        posicaoAtual = fim;
        return topicos.subList(inicio, fim);
    }
    
    /**
     * Retorna o número da página atual
     */
    public int getPaginaAtual() {
        return (posicaoAtual / tamanhoPagina) + 1;
    }
    
    /**
     * Retorna o total de páginas
     */
    public int getTotalPaginas() {
        return (int) Math.ceil((double) topicos.size() / tamanhoPagina);
    }
}
