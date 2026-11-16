package dev.com.sigea.dominio.auditoria;

/**
 * Tipos de evento do log de seguranca (Fase 4). A lista cobre os cinco
 * bullets de Big Data do enunciado: operacoes criptograficas, tentativas de
 * acesso, deteccao de anomalias, integridade e o uso das ferramentas de
 * estudo (esteganografia e ataques).
 *
 * O nome vai para a coluna tipo_evento (VARCHAR(50)) e entra no hash da
 * cadeia; renomear uma constante invalida a verificacao de todo registro ja
 * gravado com o nome antigo. Por isso o banco guarda o texto, nao o ordinal.
 */
public enum TipoEventoSeguranca {
    CIFRAGEM_OK,
    CIFRAGEM_FALHA,
    DECIFRAGEM_OK,
    DECIFRAGEM_FALHA,
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    ESTEGO_EMBUTIR,
    ESTEGO_EXTRAIR,
    ATAQUE_EXECUTADO,
    ANOMALIA_DETECTADA,
    INTEGRIDADE_VIOLADA
}
