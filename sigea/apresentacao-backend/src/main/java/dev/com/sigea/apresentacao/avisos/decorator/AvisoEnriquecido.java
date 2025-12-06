package dev.com.sigea.apresentacao.avisos.decorator;

/**
 * Decorator Pattern - Interface para avisos enriquecidos
 */
public interface AvisoEnriquecido {
    String getTitulo();
    String getConteudo();
    String getAutorId();
    String getDisciplinaId();
    String getDetalhesCompletos();
}
