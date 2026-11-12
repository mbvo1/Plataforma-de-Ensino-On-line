package dev.com.sigea.dominio.auditoria;

/**
 * Percorre a cadeia em ordem de log_id, uma linha por vez, sem guardar a
 * tabela na memoria: com 500 mil linhas, carregar tudo numa lista custaria
 * centenas de MB num container do Railway.
 *
 * Duas checagens por linha, nesta ordem:
 *   1. ENCADEAMENTO_QUEBRADO: hash_anterior nao e o hash_atual da linha
 *      anterior (ou os 64 zeros, na primeira). Pega edicao do proprio
 *      hash_anterior e linha apagada no meio - neste caso o log_id
 *      apontado e o da linha SEGUINTE a apagada, porque a apagada nao
 *      existe mais para ser apontada; ultimoLogIdIntegro mostra o buraco.
 *   2. HASH_DIVERGENTE: o hash recalculado dos campos atuais nao bate com
 *      hash_atual. Pega edicao de qualquer coluna, inclusive do hash_atual.
 *
 * Para na primeira violacao: o que vem depois foi encadeado em cima de um
 * valor ja adulterado e nao diz mais nada confiavel.
 */
public final class VerificadorCadeiaLog {

    public enum Motivo { ENCADEAMENTO_QUEBRADO, HASH_DIVERGENTE }

    public record Resultado(boolean integro, long registrosVerificados, Long logIdCorrompido, Motivo motivo,
                            Long ultimoLogIdIntegro, String hashEsperado, String hashEncontrado, String hashFinal) {
    }

    private String hashAnteriorEsperado = CadeiaHashLog.HASH_GENESE;
    private long verificados;
    private Long ultimoIntegro;
    private Resultado violacao;

    /** @return false quando a cadeia ja esta quebrada e as linhas seguintes nao serao mais examinadas. */
    public boolean aceitar(RegistroLogSeguranca registro) {
        if (violacao != null) {
            return false;
        }
        verificados++;
        if (!hashAnteriorEsperado.equals(registro.hashAnterior())) {
            violacao = new Resultado(false, verificados, registro.logId(), Motivo.ENCADEAMENTO_QUEBRADO,
                    ultimoIntegro, hashAnteriorEsperado, registro.hashAnterior(), null);
            return false;
        }
        String recalculado = registro.hashRecalculado();
        if (!recalculado.equals(registro.hashAtual())) {
            violacao = new Resultado(false, verificados, registro.logId(), Motivo.HASH_DIVERGENTE,
                    ultimoIntegro, recalculado, registro.hashAtual(), null);
            return false;
        }
        hashAnteriorEsperado = registro.hashAtual();
        ultimoIntegro = registro.logId();
        return true;
    }

    public Resultado resultado() {
        if (violacao != null) {
            return violacao;
        }
        return new Resultado(true, verificados, null, null, ultimoIntegro, null, null, hashAnteriorEsperado);
    }
}
