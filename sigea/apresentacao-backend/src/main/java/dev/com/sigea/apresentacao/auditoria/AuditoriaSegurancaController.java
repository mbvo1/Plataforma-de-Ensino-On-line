package dev.com.sigea.apresentacao.auditoria;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.RegistroLogSeguranca;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.VerificadorCadeiaLog;
import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais;
import dev.com.sigea.infraestrutura.auditoria.ConsultaLogSeguranca;
import dev.com.sigea.infraestrutura.auditoria.RegistradorLogSeguranca;
import dev.com.sigea.infraestrutura.auditoria.SeedEventosExperimentais;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Fase 4: endpoints do log de seguranca. Todos restritos a ADMINISTRADOR.
 *
 * A exportacao e a ponte para o Python da Fase 5, que nao le o .mv.db do H2
 * direto (o arquivo fica travado pelo app em execucao e o formato e interno
 * do H2). Ela escreve direto na resposta, linha a linha, em vez de montar a
 * lista: 500 mil eventos em memoria custariam centenas de MB. Nao usa
 * StreamingResponseBody de proposito - ele termina num dispatch assincrono,
 * e o JwtFiltro (OncePerRequestFilter) nao roda de novo nesse dispatch, o
 * que derrubaria a autenticacao no meio da resposta.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaSegurancaController {

    static final int MAXIMO_ANOMALIAS_POR_REQUISICAO = 10_000;
    private static final Pattern MODELO_VALIDO = Pattern.compile("[A-Za-z0-9_.-]{1,50}");
    private static final DateTimeFormatter FORMATO_UTC = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'");
    // LocalDateTime.toString omite os segundos quando sao zero ("08:05"); formato fixo para quem le o log.
    private static final DateTimeFormatter FORMATO_JANELA = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

    private final RegistradorLogSeguranca registrador;
    private final ConsultaLogSeguranca consulta;
    private final SeedEventosExperimentais seeder;
    private final ObjectMapper json;
    private final boolean seedPorEndpointHabilitado;

    public AuditoriaSegurancaController(RegistradorLogSeguranca registrador, ConsultaLogSeguranca consulta,
                                        SeedEventosExperimentais seeder, ObjectMapper json,
                                        @Value("${sigea.experimento.seed.endpoint-habilitado:false}") boolean seedPorEndpointHabilitado) {
        this.registrador = registrador;
        this.consulta = consulta;
        this.seeder = seeder;
        this.json = json;
        this.seedPorEndpointHabilitado = seedPorEndpointHabilitado;
    }

    // ------------------------------------------------------------------ integridade

    public record RespostaIntegridade(String status, long registrosVerificados, Long logIdCorrompido, String motivo,
                                      Long ultimoLogIdIntegro, String hashEsperado, String hashEncontrado,
                                      String hashFinal) {
    }

    /**
     * Percorre a cadeia e devolve o primeiro log_id corrompido, ou OK.
     * hashFinal (so quando OK) e a ancora: guardado fora do banco, e o que
     * permite notar truncamento da cauda ou a cadeia inteira recalculada,
     * que o SHA-256 sem chave nao pega sozinho.
     *
     * Quando acha corrupcao, a propria verificacao vira um evento
     * INTEGRIDADE_VIOLADA: quem adulterou o log precisa adulterar tambem o
     * registro de que a adulteracao foi vista.
     */
    @GetMapping("/auditoria/verificar-integridade")
    public RespostaIntegridade verificarIntegridade(Authentication autenticacao, HttpServletRequest requisicao)
            throws IOException {
        VerificadorCadeiaLog.Resultado r = consulta.verificarIntegridade();
        if (!r.integro()) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("log_id_corrompido", r.logIdCorrompido());
            detalhe.put("motivo", r.motivo().name());
            detalhe.put("ultimo_log_id_integro", r.ultimoLogIdIntegro());
            registrarSemDerrubar(new NovoEventoSeguranca(TipoEventoSeguranca.INTEGRIDADE_VIOLADA, idDoAdmin(autenticacao),
                    "log_seguranca/" + r.logIdCorrompido(), requisicao.getRemoteAddr(), ResultadoEvento.ALERTA,
                    json.writeValueAsString(detalhe)));
        }
        return new RespostaIntegridade(r.integro() ? "OK" : "CORROMPIDO", r.registrosVerificados(), r.logIdCorrompido(),
                r.motivo() == null ? null : r.motivo().name(), r.ultimoLogIdIntegro(), r.hashEsperado(),
                r.hashEncontrado(), r.hashFinal());
    }

    // ------------------------------------------------------------------ exportacao

    /** desde inclusivo, ate exclusivo, ambos em UTC (ISO-8601, sem fuso) e opcionais. */
    @GetMapping("/logs/export")
    public void exportar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
                         @RequestParam(defaultValue = "csv") String formato,
                         HttpServletResponse resposta) throws IOException {
        if (!formato.equals("csv") && !formato.equals("json")) {
            resposta.sendError(HttpStatus.BAD_REQUEST.value(), "formato deve ser csv ou json");
            return;
        }
        if (desde != null && ate != null && !desde.isBefore(ate)) {
            resposta.sendError(HttpStatus.BAD_REQUEST.value(), "desde deve ser anterior a ate");
            return;
        }
        prepararDownload(resposta, "log-seguranca", formato);
        if (formato.equals("csv")) {
            Writer saida = new BufferedWriter(new OutputStreamWriter(resposta.getOutputStream(), StandardCharsets.UTF_8));
            saida.write("log_id,timestamp,tipo_evento,usuario_id,recurso,ip,resultado,detalhe,hash_anterior,hash_atual\n");
            consulta.exportar(desde, ate, r -> escreverCsv(saida, r.logId(), utc(r.timestamp()), r.tipoEvento(),
                    r.usuarioId(), r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()));
            saida.flush();
        } else {
            try (JsonGenerator g = json.getFactory().createGenerator(resposta.getOutputStream())) {
                g.writeStartArray();
                consulta.exportar(desde, ate, r -> escreverJson(g, r));
                g.writeEndArray();
            }
        }
    }

    // ------------------------------------------------------------------ anomalias da Fase 5

    /** Uma janela de 5 minutos que o detector marcou. */
    public record AnomaliaDetectada(Long usuarioId, LocalDateTime inicioJanela, LocalDateTime fimJanela, Double score,
                                    String modelo) {
    }

    public record RespostaAnomalias(int registrados, Long primeiroLogId, Long ultimoLogId) {
    }

    /**
     * Recebe os scores do Python e grava cada um como ANOMALIA_DETECTADA,
     * na mesma cadeia. Validacao estrita porque o corpo vem de fora do app:
     * tamanho do lote limitado, score finito, nome de modelo restrito a um
     * alfabeto seguro (ele vai para o detalhe e, na Fase 6, para a tela).
     */
    @PostMapping("/anomalias")
    public ResponseEntity<?> registrarAnomalias(@RequestBody List<AnomaliaDetectada> anomalias,
                                                Authentication autenticacao, HttpServletRequest requisicao)
            throws IOException {
        if (anomalias == null || anomalias.isEmpty() || anomalias.size() > MAXIMO_ANOMALIAS_POR_REQUISICAO) {
            return erro(HttpStatus.BAD_REQUEST, "envie entre 1 e " + MAXIMO_ANOMALIAS_POR_REQUISICAO + " anomalias");
        }
        Long admin = idDoAdmin(autenticacao);
        List<NovoEventoSeguranca> eventos = new ArrayList<>(anomalias.size());
        for (int i = 0; i < anomalias.size(); i++) {
            AnomaliaDetectada a = anomalias.get(i);
            String problema = validar(a);
            if (problema != null) {
                return erro(HttpStatus.BAD_REQUEST, "anomalia " + i + ": " + problema);
            }
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("modelo", a.modelo());
            detalhe.put("score", a.score());
            String inicio = FORMATO_JANELA.format(a.inicioJanela());
            String fim = FORMATO_JANELA.format(a.fimJanela());
            detalhe.put("janela_inicio", inicio);
            detalhe.put("janela_fim", fim);
            detalhe.put("reportado_por", admin);
            eventos.add(new NovoEventoSeguranca(TipoEventoSeguranca.ANOMALIA_DETECTADA, a.usuarioId(),
                    "janela:" + inicio + "/" + fim, requisicao.getRemoteAddr(),
                    ResultadoEvento.ALERTA, json.writeValueAsString(detalhe)));
        }
        List<Long> ids = registrador.registrarTodos(eventos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespostaAnomalias(ids.size(), ids.get(0), ids.get(ids.size() - 1)));
    }

    private static String validar(AnomaliaDetectada a) {
        if (a == null) {
            return "item nulo";
        }
        if (a.score() == null || !Double.isFinite(a.score())) {
            return "score ausente ou nao finito";
        }
        if (a.modelo() == null || !MODELO_VALIDO.matcher(a.modelo()).matches()) {
            return "modelo deve ter 1 a 50 caracteres entre letras, digitos, _ . -";
        }
        if (a.inicioJanela() == null || a.fimJanela() == null || !a.inicioJanela().isBefore(a.fimJanela())) {
            return "inicioJanela e fimJanela obrigatorios, com inicio antes do fim";
        }
        return null;
    }

    // ------------------------------------------------------------------ volume experimental

    @PostMapping("/experimento/seed")
    public ResponseEntity<?> seed(@RequestParam(defaultValue = "" + GeradorEventosExperimentais.QUANTIDADE_PADRAO) int quantidade,
                                  @RequestParam(defaultValue = "" + GeradorEventosExperimentais.SEMENTE_PADRAO) long semente) {
        if (!seedPorEndpointHabilitado) {
            return erro(HttpStatus.FORBIDDEN, "seed por endpoint desabilitado: ele apaga o log de seguranca inteiro. "
                    + "Use o perfil seed-eventos ou sigea.experimento.seed.endpoint-habilitado=true");
        }
        try {
            return ResponseEntity.ok(seeder.executar(quantidade, semente));
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Verdade conhecida, separada da exportacao de eventos: o detector nao
     * pode recebe-la como entrada, so a avaliacao.
     */
    @GetMapping("/experimento/rotulos")
    public void rotulos(@RequestParam(defaultValue = "csv") String formato, HttpServletResponse resposta) throws IOException {
        if (!formato.equals("csv") && !formato.equals("json")) {
            resposta.sendError(HttpStatus.BAD_REQUEST.value(), "formato deve ser csv ou json");
            return;
        }
        prepararDownload(resposta, "rotulos-experimentais", formato);
        if (formato.equals("csv")) {
            Writer saida = new BufferedWriter(new OutputStreamWriter(resposta.getOutputStream(), StandardCharsets.UTF_8));
            saida.write("log_id,incidente_id,cenario,usuario_id,timestamp\n");
            consulta.rotulos(r -> escreverCsv(saida, r.logId(), r.incidenteId(), r.cenario(), r.usuarioId(), utc(r.timestamp())));
            saida.flush();
        } else {
            try (JsonGenerator g = json.getFactory().createGenerator(resposta.getOutputStream())) {
                g.writeStartArray();
                consulta.rotulos(r -> {
                    try {
                        g.writeStartObject();
                        g.writeNumberField("log_id", r.logId());
                        g.writeNumberField("incidente_id", r.incidenteId());
                        g.writeStringField("cenario", r.cenario());
                        campoLong(g, "usuario_id", r.usuarioId());
                        g.writeStringField("timestamp", utc(r.timestamp()));
                        g.writeEndObject();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                g.writeEndArray();
            }
        }
    }

    // ------------------------------------------------------------------ apoio

    private void registrarSemDerrubar(NovoEventoSeguranca evento) {
        try {
            registrador.registrar(evento);
        } catch (RuntimeException e) {
            // A resposta da verificacao vale mais que o registro dela.
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Falha ao registrar {}", evento.tipo(), e);
        }
    }

    private static Long idDoAdmin(Authentication autenticacao) {
        try {
            return autenticacao == null ? null : Long.valueOf(autenticacao.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }

    private static void prepararDownload(HttpServletResponse resposta, String nome, String formato) {
        resposta.setCharacterEncoding("UTF-8");
        resposta.setContentType(formato.equals("csv") ? "text/csv;charset=UTF-8" : "application/json");
        resposta.setHeader("Content-Disposition", "attachment; filename=\"" + nome + "." + formato + "\"");
    }

    private static String utc(LocalDateTime t) {
        return t == null ? null : FORMATO_UTC.format(t);
    }

    private void escreverJson(JsonGenerator g, RegistroLogSeguranca r) {
        try {
            g.writeStartObject();
            g.writeNumberField("log_id", r.logId());
            g.writeStringField("timestamp", utc(r.timestamp()));
            g.writeStringField("tipo_evento", r.tipoEvento());
            campoLong(g, "usuario_id", r.usuarioId());
            g.writeStringField("recurso", r.recurso());
            g.writeStringField("ip", r.ip());
            g.writeStringField("resultado", r.resultado());
            // Texto, nao objeto embutido: uma linha editada a mao pode ter JSON invalido aqui.
            g.writeStringField("detalhe", r.detalhe());
            g.writeStringField("hash_anterior", r.hashAnterior());
            g.writeStringField("hash_atual", r.hashAtual());
            g.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void campoLong(JsonGenerator g, String nome, Long valor) throws IOException {
        if (valor == null) {
            g.writeNullField(nome);
        } else {
            g.writeNumberField(nome, valor);
        }
    }

    /** RFC 4180: aspas em volta quando ha virgula, aspas ou quebra de linha; aspas internas dobradas. */
    static void escreverCsv(Writer saida, Object... campos) {
        try {
            for (int i = 0; i < campos.length; i++) {
                if (i > 0) {
                    saida.write(',');
                }
                Object c = campos[i];
                if (c == null) {
                    continue;
                }
                String s = c.toString();
                if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
                    saida.write('"');
                    saida.write(s.replace("\"", "\"\""));
                    saida.write('"');
                } else {
                    saida.write(s);
                }
            }
            saida.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
