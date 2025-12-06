package dev.com.sigea.apresentacao.foruns.observer;

import dev.com.sigea.dominio.forum.Topico;

/**
 * Observer Pattern - Interface para observadores de eventos do fórum
 */
public interface ForumObserver {
    void onNovoTopicoCriado(Topico topico);
    void onNovaResposta(String topicoId, String resposta);
}
