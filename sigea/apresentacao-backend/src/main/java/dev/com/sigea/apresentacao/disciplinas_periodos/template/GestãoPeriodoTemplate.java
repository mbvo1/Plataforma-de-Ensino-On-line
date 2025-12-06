package dev.com.sigea.apresentacao.disciplinas_periodos.template;

/**
 * Template Method Pattern - Fluxo de criação/ativação de período
 */
public abstract class GestãoPeriodoTemplate {
    
    public final String criarPeriodo(String nome, String dataInicio, String dataFim) {
        validarDatas(dataInicio, dataFim);
        checarSobreposicao(dataInicio, dataFim);
        
        String periodoId = salvar(nome, dataInicio, dataFim);
        
        emitirEventos(periodoId);
        
        return periodoId;
    }
    
    protected abstract void validarDatas(String dataInicio, String dataFim);
    protected abstract void checarSobreposicao(String dataInicio, String dataFim);
    protected abstract String salvar(String nome, String dataInicio, String dataFim);
    protected abstract void emitirEventos(String periodoId);
}
