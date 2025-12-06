package dev.com.sigea.apresentacao.avisos.observer;

/**
 * Observer Pattern - Registra marcações de leitura
 */
public class RegistroLeituraObserver implements AvisoObserver {
    
    @Override
    public void onAvisoPublicado(String avisoId, String titulo, String escopo) {
        System.out.println("📋 Registro: Criando registros de leitura para aviso " + avisoId);
        
        // Aqui seria implementada a lógica real:
        // - Criar registros vazios para todos os alunos do escopo
        // - Permitir rastreamento de quem leu e quem não leu
    }
    
    @Override
    public void onAvisoLido(String usuarioId, String avisoId) {
        System.out.println("📋 Registro: Marcando leitura - Usuário " + usuarioId + 
                         " | Aviso " + avisoId);
        
        // Aqui seria implementada a lógica real:
        // - Persistir data/hora da leitura
        // - Atualizar status para "lido"
    }
}
