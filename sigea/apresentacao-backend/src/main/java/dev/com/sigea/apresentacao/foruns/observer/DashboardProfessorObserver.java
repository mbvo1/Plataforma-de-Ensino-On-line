package dev.com.sigea.apresentacao.foruns.observer;

import dev.com.sigea.dominio.forum.Topico;

/**
 * Observer Pattern - Notifica professor no dashboard sobre novos tópicos
 */
public class DashboardProfessorObserver implements ForumObserver {
    
    @Override
    public void onNovoTopicoCriado(Topico topico) {
        System.out.println("📢 Dashboard Professor: Novo tópico criado - '" + 
                         topico.getTitulo() + "' por aluno " + topico.getAutorId());
        
        // Aqui seria implementada a lógica real:
        // - Incrementar contador de notificações
        // - Adicionar ao feed de atividades
        // - Enviar notificação em tempo real (WebSocket)
    }
    
    @Override
    public void onNovaResposta(String topicoId, String resposta) {
        System.out.println("📢 Dashboard Professor: Nova resposta no tópico " + topicoId);
    }
}
