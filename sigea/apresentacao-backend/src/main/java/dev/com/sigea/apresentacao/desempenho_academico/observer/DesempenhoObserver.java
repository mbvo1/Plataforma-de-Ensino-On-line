package dev.com.sigea.apresentacao.desempenho_academico.observer;

import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Observer Pattern - Interface para observadores de mudanças no desempenho acadêmico
 */
public interface DesempenhoObserver {
    
    /**
     * Notificado quando uma nota é lançada
     */
    void onNotaLancada(UsuarioId alunoId, String avaliacao, double nota);
    
    /**
     * Notificado quando a frequência é registrada
     */
    void onFrequenciaRegistrada(UsuarioId alunoId, int faltas);
}
