package dev.com.sigea.apresentacao.disciplinas_periodos.observer;

/**
 * Observer Pattern - Interface para mudanças de período
 */
public interface PeriodoObserver {
    void onPeriodoAtivado(String periodoId, String nome);
    void onPeriodoDesativado(String periodoId);
}
