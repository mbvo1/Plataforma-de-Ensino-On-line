package dev.com.sigea.dominio.compartilhado.padroes.templatemethod;

/**
 * Template Method para processamento de entidades.
 */
public abstract class ProcessadorEntidade<T> {
    
    /**
     * Template method que define o algoritmo de processamento.
     */
    public final void processar(T entidade) {
        validar(entidade);
        antesDeProcessar(entidade);
        executarProcessamento(entidade);
        depoisDeProcessar(entidade);
    }
    
    protected void validar(T entidade) {
        if (entidade == null) {
            throw new IllegalArgumentException("Entidade não pode ser nula.");
        }
    }
    
    protected void antesDeProcessar(T entidade) {
        // Hook method - pode ser sobrescrito
    }
    
    protected abstract void executarProcessamento(T entidade);
    
    protected void depoisDeProcessar(T entidade) {
        // Hook method - pode ser sobrescrito
    }
}

