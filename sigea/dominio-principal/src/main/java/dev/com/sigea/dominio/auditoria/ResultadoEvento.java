package dev.com.sigea.dominio.auditoria;

/**
 * Valor da coluna resultado (VARCHAR(20)). ALERTA e para eventos que nao
 * sao sucesso nem falha de uma operacao do usuario, e sim um aviso do
 * proprio sistema: anomalia detectada, cadeia violada.
 */
public enum ResultadoEvento {
    SUCESSO,
    FALHA,
    ALERTA
}
