package dev.com.sigea.dominio.turma;

import dev.com.sigea.dominio.usuario.UsuarioId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Envio {
    private final EnvioId id;
    private final UsuarioId alunoId;
    private final List<Anexo> anexos;
    private final LocalDateTime dataEnvio;
    private EnvioStatus status;

    public Envio(EnvioId id, UsuarioId alunoId, List<Anexo> anexos, EnvioStatus status) {
        this.id = Objects.requireNonNull(id);
        this.alunoId = Objects.requireNonNull(alunoId);
        this.anexos = Objects.requireNonNull(anexos);
        this.status = Objects.requireNonNull(status);
        this.dataEnvio = LocalDateTime.now();
    }

    public void marcarComoCorrigido() {
        if (this.status == EnvioStatus.CORRIGIDO) {
            throw new IllegalStateException("Envio já foi corrigido e não pode ser alterado.");
        }
        this.status = EnvioStatus.CORRIGIDO;
    }

    public void marcarComoAtrasado() {
        if (this.status == EnvioStatus.CORRIGIDO) {
            throw new IllegalStateException("Envio já foi corrigido e não pode ser alterado.");
        }
        this.status = EnvioStatus.ATRASADO;
    }

    public EnvioId getId() { return id; }
    public UsuarioId getAlunoId() { return alunoId; }
    public List<Anexo> getAnexos() { return List.copyOf(anexos); }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public EnvioStatus getStatus() { return status; }
}