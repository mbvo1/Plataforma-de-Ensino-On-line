package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Decorator Pattern - Adiciona indicadores de prazo para materiais de estudo
 */
public class MaterialComIndicadorPrazo extends MaterialDecorator {
    
    private final LocalDateTime dataLimiteEstudo;
    
    public MaterialComIndicadorPrazo(MaterialEnriquecido material, LocalDateTime dataLimiteEstudo) {
        super(material);
        this.dataLimiteEstudo = dataLimiteEstudo;
    }
    
    @Override
    public String getInformacoesAdicionais() {
        String indicador = calcularIndicadorPrazo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        return materialWrapped.getInformacoesAdicionais() + 
               "\nPrazo recomendado para estudo: " + dataLimiteEstudo.format(formatter) +
               "\nStatus: " + indicador;
    }
    
    private String calcularIndicadorPrazo() {
        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), dataLimiteEstudo);
        
        if (diasRestantes < 0) {
            return "⚠️ PRAZO EXPIRADO";
        } else if (diasRestantes <= 2) {
            return "🔴 URGENTE (" + diasRestantes + " dias restantes)";
        } else if (diasRestantes <= 7) {
            return "🟡 ATENÇÃO (" + diasRestantes + " dias restantes)";
        } else {
            return "🟢 NO PRAZO (" + diasRestantes + " dias restantes)";
        }
    }
    
    public LocalDateTime getDataLimiteEstudo() {
        return dataLimiteEstudo;
    }
}
