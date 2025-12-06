package dev.com.sigea.apresentacao.desempenho_academico.observer;

import dev.com.sigea.dominio.usuario.UsuarioId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer Pattern - Registra log de auditoria quando notas/faltas mudam
 */
public class AuditoriaObserver implements DesempenhoObserver {
    
    @Override
    public void onNotaLancada(UsuarioId alunoId, String avaliacao, double nota) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println("📝 [AUDITORIA] " + timestamp + 
                         " - Nota lançada: Aluno=" + alunoId + 
                         ", Avaliação=" + avaliacao + 
                         ", Nota=" + nota);
        
        // Aqui seria implementado o registro real no banco de dados de auditoria
    }
    
    @Override
    public void onFrequenciaRegistrada(UsuarioId alunoId, int faltas) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println("📝 [AUDITORIA] " + timestamp + 
                         " - Frequência registrada: Aluno=" + alunoId + 
                         ", Faltas=" + faltas);
    }
}
