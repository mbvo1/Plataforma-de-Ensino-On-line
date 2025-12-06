package dev.com.sigea.apresentacao.avisos.decorator;

/**
 * Decorator Pattern - Adiciona escopo (público-alvo) ao aviso
 */
public class AvisoComEscopo extends AvisoDecorator {
    
    private final String escopo; // TURMA, DISCIPLINA, GERAL
    
    public AvisoComEscopo(AvisoEnriquecido avisoDecorado, String escopo) {
        super(avisoDecorado);
        this.escopo = escopo;
    }
    
    public String getEscopo() {
        return escopo;
    }
    
    @Override
    public String getDetalhesCompletos() {
        return String.format("%s\nPúblico: %s", 
            avisoDecorado.getDetalhesCompletos(), 
            getDescricaoEscopo());
    }
    
    private String getDescricaoEscopo() {
        return switch (escopo) {
            case "TURMA" -> "Alunos da turma específica";
            case "DISCIPLINA" -> "Todos os alunos da disciplina";
            case "GERAL" -> "Todos os usuários";
            default -> "Não especificado";
        };
    }
}
