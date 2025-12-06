package dev.com.sigea.apresentacao.desempenho_academico.observer;

import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Observer Pattern - Notifica aluno via dashboard quando notas/faltas mudam
 */
public class DashboardAlunoObserver implements DesempenhoObserver {
    
    @Override
    public void onNotaLancada(UsuarioId alunoId, String avaliacao, double nota) {
        System.out.println("📊 Dashboard atualizado: Aluno " + alunoId + 
                         " recebeu nota " + nota + " em " + avaliacao);
        
        // Aqui seria implementada a lógica real de atualização do dashboard
        // Por exemplo: enviar notificação WebSocket, atualizar cache, etc.
    }
    
    @Override
    public void onFrequenciaRegistrada(UsuarioId alunoId, int faltas) {
        System.out.println("📊 Dashboard atualizado: Aluno " + alunoId + 
                         " agora tem " + faltas + " faltas");
        
        if (faltas > 15) {
            System.out.println("⚠️ ALERTA: Aluno " + alunoId + " ultrapassou 15 faltas!");
        }
    }
}
