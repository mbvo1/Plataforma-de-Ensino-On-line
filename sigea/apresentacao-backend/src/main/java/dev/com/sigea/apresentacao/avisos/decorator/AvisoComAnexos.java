package dev.com.sigea.apresentacao.avisos.decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator Pattern - Adiciona anexos ao aviso
 */
public class AvisoComAnexos extends AvisoDecorator {
    
    private final List<String> anexos;
    
    public AvisoComAnexos(AvisoEnriquecido avisoDecorado, List<String> anexos) {
        super(avisoDecorado);
        this.anexos = new ArrayList<>(anexos);
    }
    
    public List<String> getAnexos() {
        return new ArrayList<>(anexos);
    }
    
    @Override
    public String getDetalhesCompletos() {
        StringBuilder builder = new StringBuilder(avisoDecorado.getDetalhesCompletos());
        
        if (!anexos.isEmpty()) {
            builder.append("\n📎 Anexos (").append(anexos.size()).append("):");
            anexos.forEach(anexo -> builder.append("\n  - ").append(anexo));
        }
        
        return builder.toString();
    }
}
