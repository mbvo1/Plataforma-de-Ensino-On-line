package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

import dev.com.sigea.dominio.turma.Anexo;
import dev.com.sigea.dominio.turma.Material;
import java.util.List;

/**
 * Decorator Pattern - Decorator abstrato
 */
public abstract class MaterialDecorator implements MaterialEnriquecido {
    
    protected MaterialEnriquecido materialWrapped;
    
    public MaterialDecorator(MaterialEnriquecido material) {
        this.materialWrapped = material;
    }
    
    @Override
    public Material getMaterial() {
        return materialWrapped.getMaterial();
    }
    
    @Override
    public String getTitulo() {
        return materialWrapped.getTitulo();
    }
    
    @Override
    public String getDescricao() {
        return materialWrapped.getDescricao();
    }
    
    @Override
    public List<Anexo> getAnexos() {
        return materialWrapped.getAnexos();
    }
}
