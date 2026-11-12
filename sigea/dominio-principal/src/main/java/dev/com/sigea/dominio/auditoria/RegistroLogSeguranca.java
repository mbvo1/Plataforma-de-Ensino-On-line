package dev.com.sigea.dominio.auditoria;

import java.time.LocalDateTime;

/**
 * Uma linha de log_seguranca exatamente como esta no banco.
 *
 * tipoEvento e resultado sao texto, nao enum, de proposito: o verificador
 * precisa conseguir ler uma linha adulterada a mao (tipo_evento = 'XYZ') e
 * apontar a divergencia, em vez de falhar ao converter.
 *
 * timestamp e UTC, sem fuso, como o registrador grava.
 */
public record RegistroLogSeguranca(Long logId, LocalDateTime timestamp, String tipoEvento, Long usuarioId,
                                   String recurso, String ip, String resultado, String detalhe,
                                   String hashAnterior, String hashAtual) {

    /** Hash que esta linha deveria ter, recalculado a partir dos campos atuais. */
    public String hashRecalculado() {
        return CadeiaHashLog.calcular(logId, timestamp, tipoEvento, usuarioId, recurso, ip, resultado, detalhe,
                hashAnterior);
    }
}
