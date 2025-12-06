package dev.com.sigea.apresentacao.avisos.decorator;

/**
 * Decorator Pattern - Adiciona prioridade ao aviso
 */
public class AvisoComPrioridade extends AvisoDecorator {
    
    private final String prioridade; // ALTA, MEDIA, BAIXA
    
    public AvisoComPrioridade(AvisoEnriquecido avisoDecorado, String prioridade) {
        super(avisoDecorado);
        this.prioridade = prioridade;
    }
    
    public String getPrioridade() {
        return prioridade;
    }
    
    @Override
    public String getDetalhesCompletos() {
        return String.format("[%s] %s", 
            getIconePrioridade(), 
            avisoDecorado.getDetalhesCompletos());
    }
    
    private String getIconePrioridade() {
        return switch (prioridade) {
            case "ALTA" -> "🔴 URGENTE";
            case "MEDIA" -> "🟡 IMPORTANTE";
            case "BAIXA" -> "🟢 INFORMATIVO";
            default -> "ℹ️";
        };
    }
}
