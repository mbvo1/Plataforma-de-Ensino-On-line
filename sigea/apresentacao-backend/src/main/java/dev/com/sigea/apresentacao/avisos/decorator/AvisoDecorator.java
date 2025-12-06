package dev.com.sigea.apresentacao.avisos.decorator;

/**
 * Decorator Pattern - Decorador abstrato base
 */
public abstract class AvisoDecorator implements AvisoEnriquecido {
    
    protected final AvisoEnriquecido avisoDecorado;
    
    public AvisoDecorator(AvisoEnriquecido avisoDecorado) {
        this.avisoDecorado = avisoDecorado;
    }
    
    @Override
    public String getTitulo() {
        return avisoDecorado.getTitulo();
    }
    
    @Override
    public String getConteudo() {
        return avisoDecorado.getConteudo();
    }
    
    @Override
    public String getAutorId() {
        return avisoDecorado.getAutorId();
    }
    
    @Override
    public String getDisciplinaId() {
        return avisoDecorado.getDisciplinaId();
    }
    
    @Override
    public String getDetalhesCompletos() {
        return avisoDecorado.getDetalhesCompletos();
    }
}
