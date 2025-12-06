package dev.com.sigea.apresentacao.foruns.observer;

import dev.com.sigea.dominio.forum.Topico;

/**
 * Observer Pattern - Marca tópicos como lidos/não lidos
 */
public class MarcadorLeituraObserver implements ForumObserver {
    
    @Override
    public void onNovoTopicoCriado(Topico topico) {
        System.out.println("📖 Marcador: Tópico '" + topico.getTitulo() + 
                         "' marcado como NÃO LIDO para todos os participantes");
        
        // Aqui seria implementada a lógica real:
        // - Criar registros de leitura para cada aluno matriculado
        // - Marcar como não lido
    }
    
    @Override
    public void onNovaResposta(String topicoId, String resposta) {
        System.out.println("📖 Marcador: Tópico " + topicoId + 
                         " marcado como NÃO LIDO para participantes");
    }
}
