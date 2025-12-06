package dev.com.sigea.apresentacao.desempenho_academico.template;

import dev.com.sigea.dominio.sala.Nota;
import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Template Method Pattern - Define o fluxo de registro de desempenho acadêmico
 * Fluxo: registrar → validar faixa → persistir → notificar aluno
 */
public abstract class RegistroDesempenhoTemplate {
    
    /**
     * Template Method - Define o algoritmo completo
     */
    public final void registrar(UsuarioId alunoId, Object valor) {
        validarPermissao();
        validarValor(valor);
        Object valorProcessado = processarValor(valor);
        persistir(alunoId, valorProcessado);
        notificarAluno(alunoId, valorProcessado);
        executarPosProcessamento(alunoId);
    }
    
    /**
     * Hook method - pode ser sobrescrito
     */
    protected void validarPermissao() {
        // Implementação padrão vazia
    }
    
    /**
     * Método abstrato - deve ser implementado
     */
    protected abstract void validarValor(Object valor);
    
    /**
     * Método abstrato - deve ser implementado
     */
    protected abstract Object processarValor(Object valor);
    
    /**
     * Método abstrato - deve ser implementado
     */
    protected abstract void persistir(UsuarioId alunoId, Object valor);
    
    /**
     * Método abstrato - deve ser implementado
     */
    protected abstract void notificarAluno(UsuarioId alunoId, Object valor);
    
    /**
     * Hook method - pode ser sobrescrito para pós-processamento
     */
    protected void executarPosProcessamento(UsuarioId alunoId) {
        // Implementação padrão vazia
    }
}
