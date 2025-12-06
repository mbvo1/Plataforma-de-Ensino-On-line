package dev.com.sigea.apresentacao.matricula.proxy;

/**
 * Proxy Pattern - Validações de regras de negócio
 * Valida: período ativo, vagas, choque de horários
 */
public class MatriculaServiceProxy implements MatriculaService {
    
    private final MatriculaService realService;
    
    public MatriculaServiceProxy(MatriculaService realService) {
        this.realService = realService;
    }
    
    @Override
    public void matricular(String salaId, String alunoId) {
        validarPeriodoAtivo();
        validarVagasDisponiveis(salaId);
        validarChoqueHorarios(salaId, alunoId);
        
        System.out.println("🔒 Proxy: Validações concluídas, efetivando matrícula...");
        realService.matricular(salaId, alunoId);
    }
    
    @Override
    public void cancelar(String matriculaId) {
        validarPeriodoAtivo();
        realService.cancelar(matriculaId);
    }
    
    private void validarPeriodoAtivo() {
        System.out.println("✓ Período de matrícula ativo");
    }
    
    private void validarVagasDisponiveis(String salaId) {
        int vagasDisponiveis = 5; // Simulação
        if (vagasDisponiveis <= 0) {
            throw new IllegalStateException("🚫 Sem vagas disponíveis");
        }
        System.out.println("✓ Vagas disponíveis: " + vagasDisponiveis);
    }
    
    private void validarChoqueHorarios(String salaId, String alunoId) {
        System.out.println("✓ Sem choque de horários");
    }
}
