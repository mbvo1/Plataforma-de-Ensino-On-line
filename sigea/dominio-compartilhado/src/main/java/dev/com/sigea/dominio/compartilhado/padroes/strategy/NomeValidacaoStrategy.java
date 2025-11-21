package dev.com.sigea.dominio.compartilhado.padroes.strategy;

/**
 * Strategy concreta para validação de nome.
 */
public class NomeValidacaoStrategy implements ValidacaoStrategy {
    
    @Override
    public boolean validar(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        return valor.trim().length() >= 3 && valor.trim().length() <= 100;
    }
    
    @Override
    public String getMensagemErro() {
        return "Nome inválido. Deve ter entre 3 e 100 caracteres.";
    }
}

