package dev.com.sigea.dominio.compartilhado.padroes.strategy;

/**
 * Strategy concreta para validação de email.
 */
public class EmailValidacaoStrategy implements ValidacaoStrategy {
    
    @Override
    public boolean validar(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        return valor.contains("@") && valor.contains(".") && valor.length() >= 5;
    }
    
    @Override
    public String getMensagemErro() {
        return "Email inválido. Deve conter @ e ter pelo menos 5 caracteres.";
    }
}

