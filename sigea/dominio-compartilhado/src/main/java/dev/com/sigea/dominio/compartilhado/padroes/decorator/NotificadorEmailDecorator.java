package dev.com.sigea.dominio.compartilhado.padroes.decorator;

/**
 * Decorator que adiciona funcionalidade de email ao notificador.
 */
public class NotificadorEmailDecorator implements Notificador {
    
    private final Notificador notificador;
    
    public NotificadorEmailDecorator(Notificador notificador) {
        this.notificador = notificador;
    }
    
    @Override
    public void notificar(String mensagem, String destinatario) {
        notificador.notificar(mensagem, destinatario);
        System.out.println("Enviando email para " + destinatario + ": " + mensagem);
    }
}

