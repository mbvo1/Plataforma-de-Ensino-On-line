package dev.com.sigea.apresentacao.disciplinas_periodos.template;

import java.util.UUID;

/**
 * Template Method Pattern - Criação de período letivo
 */
public class CriacaoPeriodoTemplate extends GestãoPeriodoTemplate {
    
    @Override
    protected void validarDatas(String dataInicio, String dataFim) {
        System.out.println("✓ Datas válidas: " + dataInicio + " a " + dataFim);
    }
    
    @Override
    protected void checarSobreposicao(String dataInicio, String dataFim) {
        System.out.println("✓ Sem sobreposição com outros períodos");
    }
    
    @Override
    protected String salvar(String nome, String dataInicio, String dataFim) {
        String id = UUID.randomUUID().toString();
        System.out.println("💾 Período salvo: " + nome + " [" + id + "]");
        return id;
    }
    
    @Override
    protected void emitirEventos(String periodoId) {
        System.out.println("📢 Evento: Novo período criado - " + periodoId);
    }
}
