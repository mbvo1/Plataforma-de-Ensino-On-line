package dev.com.sigea.dominio.compartilhado.padroes.decorator;

/**
 * Implementação base do notificador.
 */
public class NotificadorBase implements Notificador {
    
    @Override
    public void notificar(String mensagem, String destinatario) {
        System.out.println("Notificação básica para " + destinatario + ": " + mensagem);
    }
}

