package dev.com.sigea.apresentacao.desempenho_academico.observer;

import dev.com.sigea.dominio.usuario.UsuarioId;

/**
 * Observer Pattern - Envia notificação por email quando notas/faltas mudam
 */
public class EmailNotificacaoObserver implements DesempenhoObserver {
    
    @Override
    public void onNotaLancada(UsuarioId alunoId, String avaliacao, double nota) {
        System.out.println("📧 Email enviado para aluno " + alunoId + 
                         ": Você recebeu nota " + nota + " em " + avaliacao);
        
        // Aqui seria implementado o envio real de email
        // Por exemplo: usar JavaMail, SendGrid, etc.
    }
    
    @Override
    public void onFrequenciaRegistrada(UsuarioId alunoId, int faltas) {
        if (faltas > 10) {
            System.out.println("📧 Email de alerta enviado para aluno " + alunoId + 
                             ": Você possui " + faltas + " faltas. Atenção ao limite!");
        }
    }
}
