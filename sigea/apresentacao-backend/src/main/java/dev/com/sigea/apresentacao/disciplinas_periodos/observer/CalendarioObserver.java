package dev.com.sigea.apresentacao.disciplinas_periodos.observer;

/**
 * Observer Pattern - Atualiza calendários
 */
public class CalendarioObserver implements PeriodoObserver {
    
    @Override
    public void onPeriodoAtivado(String periodoId, String nome) {
        System.out.println("📅 Calendário: Atualizando para período " + nome);
    }
    
    @Override
    public void onPeriodoDesativado(String periodoId) {
        System.out.println("📅 Calendário: Removendo período " + periodoId);
    }
}
