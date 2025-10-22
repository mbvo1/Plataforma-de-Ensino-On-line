package dev.com.sigea.dominio.ambientecolaborativo.turma;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Atividade {
    private final AtividadeId id;
    private final String titulo;
    private final String descricao;
    private final LocalDateTime dataLimite;
    private final List<Envio> envios = new ArrayList<>();

    public Atividade(AtividadeId id, String titulo, String descricao, LocalDateTime dataLimite) {
        this.id = Objects.requireNonNull(id);
        this.titulo = Objects.requireNonNull(titulo);
        this.descricao = Objects.requireNonNull(descricao);
        this.dataLimite = Objects.requireNonNull(dataLimite);
    }

    public Envio receberEnvio(UsuarioId alunoId, List<Anexo> anexos) {
        Objects.requireNonNull(alunoId);
        Objects.requireNonNull(anexos);

        var envioId = new EnvioId(UUID.randomUUID().toString());
        var agora = LocalDateTime.now();
        
        EnvioStatus status = agora.isAfter(dataLimite) ? EnvioStatus.ATRASADO : EnvioStatus.ENVIADO;
        
        var envio = new Envio(envioId, alunoId, anexos, status);
        this.envios.add(envio);
        
        return envio;
    }

    public void corrigirEnvio(EnvioId envioId) {
        var envio = envios.stream()
            .filter(e -> e.getId().equals(envioId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Envio não encontrado"));
        
        envio.marcarComoCorrigido();
    }

    public AtividadeId getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataLimite() { return dataLimite; }
    public List<Envio> getEnvios() { return List.copyOf(envios); }
}
