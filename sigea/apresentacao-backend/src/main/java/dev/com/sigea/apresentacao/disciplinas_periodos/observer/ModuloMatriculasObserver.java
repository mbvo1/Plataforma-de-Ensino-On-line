package dev.com.sigea.apresentacao.disciplinas_periodos.observer;

/**
 * Observer Pattern - Atualiza módulo de matrículas
 */
public class ModuloMatriculasObserver implements PeriodoObserver {
    
    @Override
    public void onPeriodoAtivado(String periodoId, String nome) {
        System.out.println("📝 Matrículas: Período ativo atualizado para " + nome);
        System.out.println("   Abrindo matrículas para o período " + periodoId);
    }
    
    @Override
    public void onPeriodoDesativado(String periodoId) {
        System.out.println("📝 Matrículas: Período " + periodoId + " desativado");
    }
}
