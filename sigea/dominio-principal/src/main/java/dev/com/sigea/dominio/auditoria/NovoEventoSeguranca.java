package dev.com.sigea.dominio.auditoria;

import java.util.Objects;

/**
 * Evento ainda nao gravado: o log_id, o timestamp e os hashes sao
 * atribuidos por quem grava, sob a mesma trava que serializa a cadeia.
 */
public record NovoEventoSeguranca(TipoEventoSeguranca tipo, Long usuarioId, String recurso, String ip,
                                  ResultadoEvento resultado, String detalhe) {

    public NovoEventoSeguranca {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(resultado, "resultado");
    }
}
