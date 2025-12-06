package dev.com.sigea.apresentacao.avisos.decorator;

/**
 * Decorator Pattern - Aviso base sem decorações
 */
public class AvisoBase implements AvisoEnriquecido {
    
    private final String titulo;
    private final String conteudo;
    private final String autorId;
    private final String disciplinaId;
    
    public AvisoBase(String titulo, String conteudo, String autorId, String disciplinaId) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autorId = autorId;
        this.disciplinaId = disciplinaId;
    }
    
    @Override
    public String getTitulo() {
        return titulo;
    }
    
    @Override
    public String getConteudo() {
        return conteudo;
    }
    
    @Override
    public String getAutorId() {
        return autorId;
    }
    
    @Override
    public String getDisciplinaId() {
        return disciplinaId;
    }
    
    @Override
    public String getDetalhesCompletos() {
        return String.format("Aviso: %s\n%s", titulo, conteudo);
    }
}
