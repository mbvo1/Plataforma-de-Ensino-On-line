package dev.com.sigea.dominio.compartilhado.padroes.strategy;

/**
 * Interface Strategy para validação de dados.
 */
public interface ValidacaoStrategy {
    boolean validar(String valor);
    String getMensagemErro();
}

