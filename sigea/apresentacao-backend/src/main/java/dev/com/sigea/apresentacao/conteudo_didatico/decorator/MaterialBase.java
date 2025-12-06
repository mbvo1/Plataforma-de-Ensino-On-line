package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

import dev.com.sigea.dominio.turma.Material;
import dev.com.sigea.dominio.turma.Anexo;
import java.util.List;

/**
 * Decorator Pattern - Componente concreto base
 */
public class MaterialBase implements MaterialEnriquecido {
    
    protected final Material material;
    
    public MaterialBase(Material material) {
        this.material = material;
    }
    
    @Override
    public Material getMaterial() {
        return material;
    }
    
    @Override
    public String getTitulo() {
        return material.getTitulo();
    }
    
    @Override
    public String getDescricao() {
        return "Sem descrição";
    }
    
    @Override
    public List<Anexo> getAnexos() {
        return material.getAnexos();
    }
    
    @Override
    public String getInformacoesAdicionais() {
        return "";
    }
}
