package dev.com.sigea.apresentacao.avisos.observer;

/**
 * Observer Pattern - Dissemina avisos para dashboards dos alunos
 */
public class DashboardAlunoObserver implements AvisoObserver {
    
    @Override
    public void onAvisoPublicado(String avisoId, String titulo, String escopo) {
        System.out.println("📱 Dashboard Aluno: Novo aviso publicado - " + titulo);
        System.out.println("   Escopo: " + escopo);
        System.out.println("   ID: " + avisoId);
        
        // Aqui seria implementada a lógica real:
        // - Adicionar aviso ao feed do dashboard
        // - Incrementar contador de avisos não lidos
        // - Enviar push notification
    }
    
    @Override
    public void onAvisoLido(String usuarioId, String avisoId) {
        System.out.println("✓ Dashboard Aluno: Usuário " + usuarioId + 
                         " leu aviso " + avisoId);
        
        // Aqui seria implementada a lógica real:
        // - Atualizar contador de não lidos
        // - Marcar como lido no feed
    }
}
