package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Decorator Pattern - Adiciona metadata de anexos (tamanho, tipo, data)
 */
public class MaterialComMetadata extends MaterialDecorator {
    
    private final LocalDateTime dataPublicacao;
    private final int totalAnexos;
    
    public MaterialComMetadata(MaterialEnriquecido material, LocalDateTime dataPublicacao) {
        super(material);
        this.dataPublicacao = dataPublicacao;
        this.totalAnexos = material.getAnexos().size();
    }
    
    @Override
    public String getInformacoesAdicionais() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return materialWrapped.getInformacoesAdicionais() + 
               "\nPublicado em: " + dataPublicacao.format(formatter) +
               "\nTotal de anexos: " + totalAnexos;
    }
    
    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }
    
    public int getTotalAnexos() {
        return totalAnexos;
    }
}
