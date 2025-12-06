package dev.com.sigea.apresentacao.matricula.proxy;

/**
 * Proxy Pattern - Implementação real
 */
public class MatriculaServiceReal implements MatriculaService {
    
    @Override
    public void matricular(String salaId, String alunoId) {
        System.out.println("✅ Matrícula efetivada - Sala: " + salaId + " | Aluno: " + alunoId);
    }
    
    @Override
    public void cancelar(String matriculaId) {
        System.out.println("🗑️ Matrícula cancelada - ID: " + matriculaId);
    }
}
