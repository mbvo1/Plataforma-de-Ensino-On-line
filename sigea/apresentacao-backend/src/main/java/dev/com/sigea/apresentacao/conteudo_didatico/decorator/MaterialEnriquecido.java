package dev.com.sigea.apresentacao.conteudo_didatico.decorator;

import dev.com.sigea.dominio.turma.Material;
import dev.com.sigea.dominio.turma.Anexo;
import java.util.List;

/**
 * Decorator Pattern - Component base para Material enriquecido
 */
public interface MaterialEnriquecido {
    Material getMaterial();
    String getTitulo();
    String getDescricao();
    List<Anexo> getAnexos();
    String getInformacoesAdicionais();
}
