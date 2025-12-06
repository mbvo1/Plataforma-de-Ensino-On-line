package dev.com.sigea.apresentacao.avisos.observer;

/**
 * Observer Pattern - Interface para observadores de avisos
 */
public interface AvisoObserver {
    void onAvisoPublicado(String avisoId, String titulo, String escopo);
    void onAvisoLido(String usuarioId, String avisoId);
}
