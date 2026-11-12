package dev.com.sigea.dominio.auditoria.experimento;

import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Evento sintetico do seeder, guardado em campos primitivos.
 *
 * Por que nao guardar as strings prontas: com 500 mil eventos, recurso, ip
 * e detalhe como String custariam perto de 200 MB antes da gravacao, o que
 * nao cabe com folga no container do Railway. Assim fica em torno de 30 MB,
 * e o texto e montado so na hora do INSERT.
 *
 * O cenario e o incidente sao a verdade conhecida e NAO aparecem em nenhum
 * campo gravado no log (recurso, ip, detalhe): eles vao para a tabela de
 * rotulos, a parte. Se vazassem para o detalhe, o detector da Fase 5 so
 * precisaria ler o rotulo.
 */
public final class EventoExperimental {

    static final byte RECURSO_LOGIN = 0;
    static final byte RECURSO_AVISO = 1;
    static final byte RECURSO_TOPICO = 2;
    static final byte RECURSO_ESTEGO_EMBUTIR = 3;
    static final byte RECURSO_ESTEGO_EXTRAIR = 4;
    static final byte RECURSO_ATAQUE = 5;
    static final byte RECURSO_ARQUIVO = 6;

    private static final String[] ATAQUES = {"FORCA_BRUTA", "DIFERENCIAL", "ESTATISTICO"};
    private static final String[] PERFIS = {"ALUNO", "PROFESSOR", "ADMINISTRADOR"};
    private static final TipoEventoSeguranca[] TIPOS = TipoEventoSeguranca.values();
    private static final ResultadoEvento[] RESULTADOS = ResultadoEvento.values();
    private static final GeradorEventosExperimentais.Cenario[] CENARIOS = GeradorEventosExperimentais.Cenario.values();

    final long instanteMillis;
    private final byte tipo;
    private final long usuarioId;
    private final byte perfil;
    private final byte tipoRecurso;
    private final int idRecurso;
    private final short ip;
    private final byte resultado;
    private final byte cenario;
    private final int incidente;

    EventoExperimental(long instanteMillis, TipoEventoSeguranca tipo, long usuarioId, int perfil, byte tipoRecurso,
                       int idRecurso, int ip, ResultadoEvento resultado, GeradorEventosExperimentais.Cenario cenario,
                       int incidente) {
        this.instanteMillis = instanteMillis;
        this.tipo = (byte) tipo.ordinal();
        this.usuarioId = usuarioId;
        this.perfil = (byte) perfil;
        this.tipoRecurso = tipoRecurso;
        this.idRecurso = idRecurso;
        this.ip = (short) ip;
        this.resultado = (byte) resultado.ordinal();
        this.cenario = (byte) (cenario == null ? -1 : cenario.ordinal());
        this.incidente = incidente;
    }

    /** UTC, sem fuso, truncado em milissegundos - o mesmo que a coluna devolve na leitura. */
    public LocalDateTime timestamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(instanteMillis), ZoneOffset.UTC);
    }

    public TipoEventoSeguranca tipo() {
        return TIPOS[tipo];
    }

    public long usuarioId() {
        return usuarioId;
    }

    public ResultadoEvento resultado() {
        return RESULTADOS[resultado];
    }

    public String recurso() {
        return switch (tipoRecurso) {
            case RECURSO_LOGIN -> "/api/auth/login";
            case RECURSO_AVISO -> "/api/avisos/" + idRecurso;
            case RECURSO_TOPICO -> "/api/foruns/topicos/" + idRecurso;
            case RECURSO_ESTEGO_EMBUTIR -> "/api/estego/embutir";
            case RECURSO_ESTEGO_EXTRAIR -> "/api/estego/extrair";
            case RECURSO_ATAQUE -> "ataque:" + ATAQUES[idRecurso];
            case RECURSO_ARQUIVO -> "/uploads/material-" + idRecurso + ".pdf";
            default -> throw new IllegalStateException("tipo de recurso desconhecido: " + tipoRecurso);
        };
    }

    /**
     * Enderecos dos tres blocos reservados para documentacao (RFC 5737),
     * para que nenhum IP real apareca no volume sintetico. Atacantes e
     * usuarios normais saem do mesmo conjunto: um bloco so de atacantes
     * seria uma pista que nao existe no mundo real.
     */
    public String ip() {
        int bloco = ip / 254;
        int host = ip % 254 + 1;
        return switch (bloco) {
            case 0 -> "192.0.2." + host;
            case 1 -> "198.51.100." + host;
            default -> "203.0.113." + host;
        };
    }

    /** Mesmo formato para evento normal e anomalo; ver o comentario da classe. */
    public String detalhe() {
        return "{\"origem\":\"seeder\",\"perfil\":\"" + PERFIS[perfil] + "\"}";
    }

    /** Verdade conhecida: null para evento normal. */
    public GeradorEventosExperimentais.Cenario cenario() {
        return cenario < 0 ? null : CENARIOS[cenario];
    }

    /** Id do incidente injetado, ou -1 para evento normal. */
    public int incidente() {
        return incidente;
    }

    public NovoEventoSeguranca comoNovoEvento() {
        return new NovoEventoSeguranca(tipo(), usuarioId, recurso(), ip(), resultado(), detalhe());
    }
}
