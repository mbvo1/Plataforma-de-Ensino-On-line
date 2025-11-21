package dev.com.sigea.dominio.compartilhado.padroes.decorator;

/**
 * Interface base para notificadores (Decorator pattern).
 */
public interface Notificador {
    void notificar(String mensagem, String destinatario);
}

