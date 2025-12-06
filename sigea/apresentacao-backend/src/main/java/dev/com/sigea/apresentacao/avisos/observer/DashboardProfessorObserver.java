package dev.com.sigea.apresentacao.avisos.observer;

/**
 * Observer Pattern - Dissemina avisos para dashboard do professor
 */
public class DashboardProfessorObserver implements AvisoObserver {
    
    @Override
    public void onAvisoPublicado(String avisoId, String titulo, String escopo) {
        System.out.println("👨‍🏫 Dashboard Professor: Aviso publicado com sucesso");
        System.out.println("   Título: " + titulo);
        System.out.println("   Alcance: " + escopo);
        
        // Aqui seria implementada a lógica real:
        // - Adicionar ao histórico de avisos enviados
        // - Mostrar estatísticas de visualização
    }
    
    @Override
    public void onAvisoLido(String usuarioId, String avisoId) {
        System.out.println("👨‍🏫 Dashboard Professor: +1 leitura no aviso " + avisoId);
        
        // Aqui seria implementada a lógica real:
        // - Atualizar estatísticas de leitura
        // - Mostrar quem já leu
    }
}
