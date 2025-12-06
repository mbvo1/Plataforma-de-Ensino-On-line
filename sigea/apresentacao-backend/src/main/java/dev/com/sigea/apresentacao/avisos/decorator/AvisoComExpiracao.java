package dev.com.sigea.apresentacao.avisos.decorator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Decorator Pattern - Adiciona prazo de expiração ao aviso
 */
public class AvisoComExpiracao extends AvisoDecorator {
    
    private final LocalDateTime dataExpiracao;
    
    public AvisoComExpiracao(AvisoEnriquecido avisoDecorado, LocalDateTime dataExpiracao) {
        super(avisoDecorado);
        this.dataExpiracao = dataExpiracao;
    }
    
    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }
    
    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }
    
    @Override
    public String getDetalhesCompletos() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String status = estaExpirado() ? "⌛ EXPIRADO" : "✅ ATIVO";
        
        return String.format("%s\n%s até %s", 
            avisoDecorado.getDetalhesCompletos(),
            status,
            dataExpiracao.format(formatter));
    }
}
