package dev.com.sigea.apresentacao.auditoria;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.infraestrutura.auditoria.ConsultaLogSeguranca;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Fase 6: dados do dashboard-seguranca.html. Todos restritos a ADMINISTRADOR.
 *
 * Duas origens, e a tela diz qual e qual:
 *   - o log vivo (eventos por hora, anomalias), agregado aqui no servidor -
 *     mandar 100 mil linhas ao navegador para ele contar seria 27 MB por
 *     carga de pagina;
 *   - os CSVs de evidencia (avalanche, frequencia de bytes, metricas do
 *     detector), copiados para o jar pelo Maven. Os paineis da cifra e do
 *     detector mostram exatamente os numeros medidos, sem recalcular nada -
 *     regra do brief: numero que aparece tem de sair de um CSV gerado por
 *     execucao real.
 */
@RestController
@RequestMapping("/api/admin/dashboard-seguranca")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class DashboardSegurancaController {

    /**
     * Lista fechada: o nome vem da URL e vira caminho de classpath. Sem a
     * lista, "../application.properties" seria uma leitura de arquivo
     * arbitraria do jar.
     */
    static final Set<String> EVIDENCIAS_PERMITIDAS = Set.of("avalanche.csv", "frequencia-bytes.csv", "deteccao.csv");
    static final int MAXIMO_POR_MODELO = 200;
    private static final DateTimeFormatter FORMATO_UTC = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");

    private final ConsultaLogSeguranca consulta;
    private final ObjectMapper json;

    public DashboardSegurancaController(ConsultaLogSeguranca consulta, ObjectMapper json) {
        this.consulta = consulta;
        this.json = json;
    }

    public record PontoHora(String hora, long total, long sucessos, long falhas, long alertas) {
    }

    @GetMapping("/eventos-por-hora")
    public List<PontoHora> eventosPorHora() {
        return consulta.eventosPorHora().stream()
                .map(e -> new PontoHora(FORMATO_UTC.format(e.hora()), e.total(), e.sucessos(), e.falhas(), e.alertas()))
                .toList();
    }

    public record Anomalia(long logId, String registradaEm, Long usuarioId, String modelo, Double score,
                           String janelaInicio, String janelaFim) {
    }

    public record RespostaAnomalias(long total, Map<String, Long> porModelo, List<Anomalia> maioresScores) {
    }

    /**
     * As anomalias de maior score de cada modelo. Por modelo porque os
     * scores nao sao comparaveis entre si: o do IsolationForest fica perto
     * de 0,4 a 0,8, o do z-score e um numero de desvios.
     *
     * O detalhe e JSON gravado por quem chamou POST /anomalias; linha que
     * nao parseia (editada a mao, por exemplo) aparece com campos nulos em
     * vez de derrubar o painel.
     */
    @GetMapping("/anomalias")
    public RespostaAnomalias anomalias(@RequestParam(defaultValue = "50") int porModelo) {
        int limite = Math.max(1, Math.min(porModelo, MAXIMO_POR_MODELO));
        Map<String, Long> contagem = new TreeMap<>();
        Map<String, PriorityQueue<Anomalia>> melhores = new HashMap<>();
        long[] total = {0};
        Comparator<Anomalia> porScore = Comparator.comparing(a -> a.score() == null ? Double.NEGATIVE_INFINITY : a.score());
        consulta.eventosDoTipo(TipoEventoSeguranca.ANOMALIA_DETECTADA.name(), e -> {
            total[0]++;
            Anomalia a = anomalia(e);
            String modelo = a.modelo() == null ? "(desconhecido)" : a.modelo();
            contagem.merge(modelo, 1L, Long::sum);
            PriorityQueue<Anomalia> fila = melhores.computeIfAbsent(modelo, m -> new PriorityQueue<>(porScore));
            fila.add(a);
            if (fila.size() > limite) {
                fila.poll();
            }
        });
        List<Anomalia> itens = new ArrayList<>();
        for (String modelo : contagem.keySet()) {
            List<Anomalia> doModelo = new ArrayList<>(melhores.get(modelo));
            doModelo.sort(porScore.reversed().thenComparing(Anomalia::logId));
            itens.addAll(doModelo);
        }
        return new RespostaAnomalias(total[0], contagem, itens);
    }

    private Anomalia anomalia(ConsultaLogSeguranca.EventoResumido e) {
        String modelo = null;
        Double score = null;
        String inicio = null;
        String fim = null;
        try {
            JsonNode d = e.detalhe() == null ? null : json.readTree(e.detalhe());
            if (d != null && d.isObject()) {
                modelo = d.hasNonNull("modelo") ? d.get("modelo").asText() : null;
                score = d.hasNonNull("score") && d.get("score").isNumber() ? d.get("score").asDouble() : null;
                inicio = d.hasNonNull("janela_inicio") ? d.get("janela_inicio").asText() : null;
                fim = d.hasNonNull("janela_fim") ? d.get("janela_fim").asText() : null;
            }
        } catch (IOException ignorado) {
            // detalhe invalido: campos ficam nulos
        }
        LocalDateTime t = e.timestamp();
        return new Anomalia(e.logId(), t == null ? null : FORMATO_UTC.format(t), e.usuarioId(), modelo, score, inicio, fim);
    }

    @GetMapping(value = "/evidencias/{nome}", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> evidencia(@PathVariable String nome) throws IOException {
        if (!EVIDENCIAS_PERMITIDAS.contains(nome)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body("evidencia nao disponivel");
        }
        ClassPathResource recurso = new ClassPathResource("evidencias/projeto-2/" + nome);
        if (!recurso.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN)
                    .body("evidencia nao empacotada neste build: " + nome);
        }
        try (InputStream in = recurso.getInputStream()) {
            return ResponseEntity.ok(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
