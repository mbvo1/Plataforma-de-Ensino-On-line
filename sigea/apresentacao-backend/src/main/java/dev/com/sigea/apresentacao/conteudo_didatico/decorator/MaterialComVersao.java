package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

/**
 * Decorator Pattern - Adiciona versionamento ao material
 */
public class MaterialComVersao extends MaterialDecorator {
    
    private final String versao;
    
    public MaterialComVersao(MaterialEnriquecido material, String versao) {
        super(material);
        this.versao = versao;
    }
    
    @Override
    public String getInformacoesAdicionais() {
        return materialWrapped.getInformacoesAdicionais() + 
               "\nVersão: " + versao;
    }
    
    public String getVersao() {
        return versao;
    }
}
