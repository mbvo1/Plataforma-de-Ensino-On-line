package dev.com.sigea.apresentacao.auditoria;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.com.sigea.apresentacao.seguranca.JwtUtil;
import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais;
import dev.com.sigea.infraestrutura.auditoria.SeedEventosExperimentais;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fase 4 de ponta a ponta: Flyway (V28, V29) num H2 em memoria, o seeder
 * gravando o volume padrao de 100 mil eventos, e os endpoints chamados com
 * JWT real pela cadeia de filtros do Spring Security.
 *
 * Criterio de aceite "alterar um registro a mao pelo console H2 e o
 * endpoint apontar exatamente aquele log_id": o console so manda SQL ao
 * banco; aqui o mesmo UPDATE vai pelo JDBC, coluna por coluna, e o endpoint
 * tem de devolver o log_id editado. A demonstracao manual pelo console esta
 * descrita no README das evidencias (perfil console-h2).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fase4;DB_CLOSE_DELAY=-1",
        "jwt.secret=segredo-so-de-teste-com-mais-de-32-bytes-0123456789",
        "gemini.api.key=teste",
        "cpf.encryption.key=chave-so-de-teste",
        "spring.jpa.show-sql=false",
        "file.upload-dir=target/uploads-teste"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogSegurancaIntegracaoTest {

    private static final long ALVO = 54_321;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SeedEventosExperimentais seeder;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JwtUtil jwt;
    @Autowired
    private ObjectMapper json;

    private String tokenAdmin;
    private long idAdmin;
    private SeedEventosExperimentais.Resultado seed;

    @BeforeAll
    void gravarVolumePadrao() throws Exception {
        idAdmin = jdbc.queryForObject("SELECT usuario_id FROM Usuarios WHERE email = 'admin@sigea.com'", Long.class);
        tokenAdmin = jwt.gerarToken(String.valueOf(idAdmin), "admin@sigea.com", "ADMINISTRADOR");

        seed = seeder.executar(GeradorEventosExperimentais.QUANTIDADE_PADRAO, GeradorEventosExperimentais.SEMENTE_PADRAO);

        long inicio = System.nanoTime();
        JsonNode r = verificar();
        long msVerificacao = (System.nanoTime() - inicio) / 1_000_000;
        assertEquals("OK", r.get("status").asText(), r.toString());

        // Tempo de parede: varia de maquina para maquina e entre execucoes,
        // como a forca bruta. O hash_final nao varia: mesma semente, mesma
        // tabela, hash por hash.
        Path csv = Paths.get(System.getProperty("evidencias.dir", "../evidencias/projeto-2")).resolve("seeder-volume.csv");
        Files.createDirectories(csv.getParent());
        Files.write(csv, List.of(
                "quantidade,semente,eventos_gravados,rotulos_gravados,incidentes,ms_geracao,ms_gravacao_h2,ms_verificacao_endpoint,hash_final",
                String.format(Locale.ROOT, "%d,%d,%d,%d,%d,%d,%d,%d,%s", seed.quantidade(), seed.semente(),
                        seed.eventosGravados(), seed.rotulosGravados(), seed.incidentes(), seed.milissegundosGeracao(),
                        seed.milissegundosGravacao(), msVerificacao, r.get("hashFinal").asText())), StandardCharsets.UTF_8);
        System.out.println("[Evidencias] " + csv.normalize());
    }

    @Test
    @DisplayName("Seeder grava 100 mil eventos e um rotulo para cada evento de incidente injetado")
    void seederGravaEventosERotulos() {
        assertEquals(100_000L, seed.eventosGravados());
        long rotuladosNoGerador = GeradorEventosExperimentais.gerar(GeradorEventosExperimentais.QUANTIDADE_PADRAO,
                GeradorEventosExperimentais.SEMENTE_PADRAO).eventos().stream().filter(e -> e.cenario() != null).count();
        assertEquals(rotuladosNoGerador, seed.rotulosGravados());
        assertEquals(rotuladosNoGerador, jdbc.queryForObject("SELECT COUNT(*) FROM rotulo_evento_experimental", Long.class));
        assertEquals(1L, jdbc.queryForObject("SELECT MIN(log_id) FROM log_seguranca WHERE log_id <= 100000", Long.class));
        assertEquals(100_000L, jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE log_id <= 100000", Long.class));
        assertEquals(50, seed.incidentes());
        assertThrows(IllegalArgumentException.class, () -> seeder.executar(99_999, 1));
        assertThrows(IllegalArgumentException.class, () -> seeder.executar(500_001, 1));
    }

    @Test
    @DisplayName("Aceite: editar qualquer coluna de uma linha a mao faz o endpoint apontar exatamente aquele log_id")
    void edicaoAMaoApontaOLogId() throws Exception {
        Map<String, String> edicoes = new LinkedHashMap<>();
        edicoes.put("timestamp", "UPDATE log_seguranca SET timestamp = DATEADD('SECOND', 1, timestamp) WHERE log_id = ?");
        edicoes.put("tipo_evento", "UPDATE log_seguranca SET tipo_evento = CASE WHEN tipo_evento = 'LOGIN_FALHA' "
                + "THEN 'LOGIN_SUCESSO' ELSE 'LOGIN_FALHA' END WHERE log_id = ?");
        edicoes.put("usuario_id", "UPDATE log_seguranca SET usuario_id = usuario_id + 1 WHERE log_id = ?");
        edicoes.put("usuario_id_nulo", "UPDATE log_seguranca SET usuario_id = NULL WHERE log_id = ?");
        edicoes.put("recurso", "UPDATE log_seguranca SET recurso = '/api/avisos/999999' WHERE log_id = ?");
        edicoes.put("ip", "UPDATE log_seguranca SET ip = '10.66.66.66' WHERE log_id = ?");
        edicoes.put("resultado", "UPDATE log_seguranca SET resultado = CASE WHEN resultado = 'SUCESSO' "
                + "THEN 'FALHA' ELSE 'SUCESSO' END WHERE log_id = ?");
        edicoes.put("detalhe", "UPDATE log_seguranca SET detalhe = '{\"origem\":\"seeder\",\"perfil\":\"ADMINISTRADOR\"}' "
                + "WHERE log_id = ?");
        edicoes.put("hash_atual", "UPDATE log_seguranca SET hash_atual = CASE WHEN LEFT(hash_atual, 1) = '0' THEN '1' "
                + "ELSE '0' END || SUBSTRING(hash_atual, 2) WHERE log_id = ?");
        edicoes.put("hash_anterior", "UPDATE log_seguranca SET hash_anterior = CASE WHEN LEFT(hash_anterior, 1) = '0' "
                + "THEN '1' ELSE '0' END || SUBSTRING(hash_anterior, 2) WHERE log_id = ?");

        long eventosAntes = contar("INTEGRIDADE_VIOLADA", null);
        int verificacoesCorrompidas = 0;
        for (long alvo : new long[]{1, ALVO, 100_000}) {
            for (Map.Entry<String, String> e : edicoes.entrySet()) {
                Map<String, Object> original = jdbc.queryForMap("SELECT * FROM log_seguranca WHERE log_id = ?", alvo);
                assertEquals(1, jdbc.update(e.getValue(), alvo), e.getKey());
                JsonNode r = verificar();
                verificacoesCorrompidas++;
                assertEquals("CORROMPIDO", r.get("status").asText(), e.getKey() + " em " + alvo);
                assertEquals(alvo, r.get("logIdCorrompido").asLong(), e.getKey() + " em " + alvo);
                assertEquals(e.getKey().equals("hash_anterior") ? "ENCADEAMENTO_QUEBRADO" : "HASH_DIVERGENTE",
                        r.get("motivo").asText(), e.getKey());
                restaurar(original);
                assertEquals("OK", verificar().get("status").asText(), "restaurado " + e.getKey() + " em " + alvo);
            }
        }
        // Cada verificacao que achou corrupcao virou um evento INTEGRIDADE_VIOLADA, encadeado como qualquer outro.
        assertEquals(eventosAntes + verificacoesCorrompidas, contar("INTEGRIDADE_VIOLADA", null));
        assertEquals("OK", verificar().get("status").asText());
    }

    @Test
    @DisplayName("Apagar uma linha do meio aponta a seguinte, com o ultimo integro mostrando o buraco")
    void linhaApagada() throws Exception {
        Map<String, Object> original = jdbc.queryForMap("SELECT * FROM log_seguranca WHERE log_id = ?", ALVO);
        jdbc.update("DELETE FROM rotulo_evento_experimental WHERE log_id = ?", ALVO);
        assertEquals(1, jdbc.update("DELETE FROM log_seguranca WHERE log_id = ?", ALVO));
        JsonNode r = verificar();
        assertEquals(ALVO + 1, r.get("logIdCorrompido").asLong());
        assertEquals(ALVO - 1, r.get("ultimoLogIdIntegro").asLong());
        assertEquals("ENCADEAMENTO_QUEBRADO", r.get("motivo").asText());
        jdbc.update("INSERT INTO log_seguranca (log_id, timestamp, tipo_evento, usuario_id, recurso, ip, resultado, detalhe, "
                        + "hash_anterior, hash_atual) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                original.get("LOG_ID"), original.get("TIMESTAMP"), original.get("TIPO_EVENTO"), original.get("USUARIO_ID"),
                original.get("RECURSO"), original.get("IP"), original.get("RESULTADO"), texto(original.get("DETALHE")),
                original.get("HASH_ANTERIOR"), original.get("HASH_ATUAL"));
        assertEquals("OK", verificar().get("status").asText());
    }

    @Test
    @DisplayName("Endpoints so para ADMINISTRADOR: sem token e com token de aluno sao recusados")
    void soAdministrador() throws Exception {
        String aluno = jwt.gerarToken("999", "aluno@sigea.com", "ALUNO");
        for (String url : List.of("/api/admin/auditoria/verificar-integridade", "/api/admin/logs/export",
                "/api/admin/experimento/rotulos", "/api/admin/dashboard-seguranca/eventos-por-hora",
                "/api/admin/dashboard-seguranca/anomalias", "/api/admin/dashboard-seguranca/evidencias/avalanche.csv")) {
            int anonimo = mvc.perform(get(url)).andReturn().getResponse().getStatus();
            assertTrue(anonimo == 401 || anonimo == 403, url + " anonimo: " + anonimo);
            mvc.perform(get(url).header("Authorization", "Bearer " + aluno)).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/admin/anomalias").header("Authorization", "Bearer " + aluno)
                .contentType(MediaType.APPLICATION_JSON).content("[]")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Exportacao CSV e JSON por periodo: mesmas linhas do banco, CSV com aspas no detalhe, sem rotulo")
    void exportacao() throws Exception {
        String desde = "2026-10-07T00:00:00";
        String ate = "2026-10-08T00:00:00";
        long esperado = jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE timestamp >= ? AND timestamp < ?",
                Long.class, java.time.LocalDateTime.parse(desde), java.time.LocalDateTime.parse(ate));
        assertTrue(esperado > 1_000, "dia util deveria ter milhares de eventos: " + esperado);

        String csv = baixar("/api/admin/logs/export?formato=csv&desde=" + desde + "&ate=" + ate);
        String[] linhas = csv.split("\n");
        assertEquals("log_id,timestamp,tipo_evento,usuario_id,recurso,ip,resultado,detalhe,hash_anterior,hash_atual", linhas[0]);
        assertEquals(esperado, linhas.length - 1);
        assertFalse(linhas[0].contains("cenario") || linhas[0].contains("incidente"), "rotulo nao pode ir na exportacao");

        long primeiro = Long.parseLong(linhas[1].substring(0, linhas[1].indexOf(',')));
        Map<String, Object> linha = jdbc.queryForMap("SELECT * FROM log_seguranca WHERE log_id = ?", primeiro);
        String detalheCsv = "\"" + texto(linha.get("DETALHE")).replace("\"", "\"\"") + "\"";
        assertTrue(linhas[1].contains("," + detalheCsv + ","), linhas[1]);
        assertTrue(linhas[1].endsWith("," + linha.get("HASH_ANTERIOR") + "," + linha.get("HASH_ATUAL")), linhas[1]);

        List<Map<String, Object>> eventos = json.readValue(
                baixar("/api/admin/logs/export?formato=json&desde=" + desde + "&ate=" + ate), new TypeReference<>() {
                });
        assertEquals(esperado, eventos.size());
        assertEquals(primeiro, ((Number) eventos.get(0).get("log_id")).longValue());
        assertEquals(texto(linha.get("DETALHE")), eventos.get(0).get("detalhe"));
        assertTrue(eventos.get(0).get("timestamp").toString().endsWith("Z"));

        mvc.perform(get("/api/admin/logs/export?formato=xml").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/logs/export?desde=" + ate + "&ate=" + desde).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rotulos saem por endpoint proprio, um por evento de incidente")
    void exportacaoDeRotulos() throws Exception {
        String csv = baixar("/api/admin/experimento/rotulos?formato=csv");
        String[] linhas = csv.split("\n");
        assertEquals("log_id,incidente_id,cenario,usuario_id,timestamp", linhas[0]);
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM rotulo_evento_experimental", Long.class), linhas.length - 1L);
        List<Map<String, Object>> json = this.json.readValue(baixar("/api/admin/experimento/rotulos?formato=json"),
                new TypeReference<>() {
                });
        assertEquals(linhas.length - 1, json.size());
    }

    @Test
    @DisplayName("POST /anomalias grava ANOMALIA_DETECTADA na cadeia e recusa lote invalido sem gravar nada")
    void anomalias() throws Exception {
        long antes = contar("ANOMALIA_DETECTADA", null);
        String corpo = "[{\"usuarioId\":100123,\"inicioJanela\":\"2026-10-07T20:20:00\",\"fimJanela\":\"2026-10-07T20:25:00\","
                + "\"score\":0.83,\"modelo\":\"isolation_forest\"},"
                + "{\"usuarioId\":null,\"inicioJanela\":\"2026-10-08T03:00:00\",\"fimJanela\":\"2026-10-08T03:05:00\","
                + "\"score\":4.2,\"modelo\":\"zscore\"}]";
        MvcResult r = mvc.perform(post("/api/admin/anomalias").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated()).andReturn();
        JsonNode resposta = json.readTree(r.getResponse().getContentAsString());
        assertEquals(2, resposta.get("registrados").asInt());
        assertEquals(antes + 2, contar("ANOMALIA_DETECTADA", null));
        Map<String, Object> gravado = jdbc.queryForMap("SELECT * FROM log_seguranca WHERE log_id = ?",
                resposta.get("primeiroLogId").asLong());
        assertEquals(100123L, ((Number) gravado.get("USUARIO_ID")).longValue());
        assertEquals("ALERTA", gravado.get("RESULTADO"));
        assertEquals("janela:2026-10-07T20:20:00/2026-10-07T20:25:00", gravado.get("RECURSO"));
        JsonNode detalhe = json.readTree(texto(gravado.get("DETALHE")));
        assertEquals("isolation_forest", detalhe.get("modelo").asText());
        assertEquals(idAdmin, detalhe.get("reportado_por").asLong());

        for (String invalido : List.of("[]",
                "[{\"usuarioId\":1,\"inicioJanela\":\"2026-10-07T20:20:00\",\"fimJanela\":\"2026-10-07T20:25:00\",\"score\":null,\"modelo\":\"m\"}]",
                "[{\"usuarioId\":1,\"inicioJanela\":\"2026-10-07T20:20:00\",\"fimJanela\":\"2026-10-07T20:25:00\",\"score\":1,\"modelo\":\"<script>\"}]",
                "[{\"usuarioId\":1,\"inicioJanela\":\"2026-10-07T20:25:00\",\"fimJanela\":\"2026-10-07T20:20:00\",\"score\":1,\"modelo\":\"m\"}]")) {
            mvc.perform(post("/api/admin/anomalias").header("Authorization", "Bearer " + tokenAdmin)
                    .contentType(MediaType.APPLICATION_JSON).content(invalido)).andExpect(status().isBadRequest());
        }
        assertEquals(antes + 2, contar("ANOMALIA_DETECTADA", null), "lote invalido nao grava nada");
        assertEquals("OK", verificar().get("status").asText());
    }

    @Test
    @DisplayName("Login real gera LOGIN_SUCESSO e LOGIN_FALHA com usuario_id quando o e-mail existe, sem gravar o e-mail")
    void eventosDeLogin() throws Exception {
        long sucessos = contar("LOGIN_SUCESSO", idAdmin);
        long falhasAdmin = contar("LOGIN_FALHA", idAdmin);
        long falhasAnonimas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento = 'LOGIN_FALHA' AND usuario_id IS NULL", Long.class);

        mvc.perform(post("/api/auth/login-admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@sigea.com\",\"senha\":\"admin123\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login-admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@sigea.com\",\"senha\":\"errada\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ninguem@exemplo.com\",\"senha\":\"x\"}")).andExpect(status().isUnauthorized());

        assertEquals(sucessos + 1, contar("LOGIN_SUCESSO", idAdmin));
        assertEquals(falhasAdmin + 1, contar("LOGIN_FALHA", idAdmin));
        assertEquals(falhasAnonimas + 1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento = 'LOGIN_FALHA' AND usuario_id IS NULL", Long.class));
        assertEquals(0L, jdbc.queryForObject(
                "SELECT COUNT(*) FROM log_seguranca WHERE CAST(detalhe AS VARCHAR) LIKE '%@%' OR recurso LIKE '%@%'", Long.class));
        assertEquals("OK", verificar().get("status").asText());
    }

    @Test
    @DisplayName("Dashboard: eventos por hora somam o log inteiro menos ANOMALIA_DETECTADA, agregados no banco")
    void dashboardEventosPorHora() throws Exception {
        JsonNode pontos = json.readTree(baixar("/api/admin/dashboard-seguranca/eventos-por-hora"));
        long soma = 0;
        for (JsonNode p : pontos) {
            assertEquals(p.get("total").asLong(),
                    p.get("sucessos").asLong() + p.get("falhas").asLong() + p.get("alertas").asLong());
            assertTrue(p.get("hora").asText().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:00:00Z"), p.toString());
            soma += p.get("total").asLong();
        }
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento <> 'ANOMALIA_DETECTADA'",
                Long.class), soma, "ANOMALIA_DETECTADA fica fora: o timestamp e o do envio, nao o do comportamento");
        assertTrue(pontos.size() > 300, "duas semanas de volume ocupam centenas de horas: " + pontos.size());
    }

    @Test
    @DisplayName("Dashboard: anomalias de maior score por modelo, ordenadas, com usuario e janela")
    void dashboardAnomalias() throws Exception {
        StringBuilder corpo = new StringBuilder("[");
        double[] scores = {0.10, 0.90, 0.50};
        for (int i = 0; i < scores.length; i++) {
            corpo.append(i > 0 ? "," : "").append("{\"usuarioId\":").append(100_200 + i)
                    .append(",\"inicioJanela\":\"2026-10-09T1").append(i).append(":00:00\",\"fimJanela\":\"2026-10-09T1")
                    .append(i).append(":05:00\",\"score\":").append(scores[i]).append(",\"modelo\":\"teste_dashboard\"}");
        }
        mvc.perform(post("/api/admin/anomalias").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(corpo.append("]").toString())).andExpect(status().isCreated());

        JsonNode r = json.readTree(baixar("/api/admin/dashboard-seguranca/anomalias?porModelo=2"));
        assertEquals(3, r.get("porModelo").get("teste_dashboard").asLong());
        List<JsonNode> doModelo = new java.util.ArrayList<>();
        r.get("maioresScores").forEach(a -> {
            if ("teste_dashboard".equals(a.get("modelo").asText())) {
                doModelo.add(a);
            }
        });
        assertEquals(2, doModelo.size(), "so os 2 maiores");
        assertEquals(0.90, doModelo.get(0).get("score").asDouble(), 1e-9);
        assertEquals(0.50, doModelo.get(1).get("score").asDouble(), 1e-9);
        assertEquals(100_201, doModelo.get(0).get("usuarioId").asLong());
        assertEquals("2026-10-09T11:00:00", doModelo.get(0).get("janelaInicio").asText());
    }

    @Test
    @DisplayName("Dashboard: evidencias so da lista fechada, e iguais ao CSV versionado")
    void dashboardEvidencias() throws Exception {
        for (String nome : List.of("avalanche.csv", "frequencia-bytes.csv", "deteccao.csv")) {
            String servido = baixar("/api/admin/dashboard-seguranca/evidencias/" + nome);
            String versionado = Files.readString(Paths.get("../evidencias/projeto-2", nome), StandardCharsets.UTF_8);
            assertEquals(versionado.replace("\r\n", "\n"), servido.replace("\r\n", "\n"), nome);
        }
        for (String proibido : List.of("application.properties", "estego.csv", "..%2Fapplication.properties")) {
            int status = mvc.perform(get("/api/admin/dashboard-seguranca/evidencias/" + proibido)
                    .header("Authorization", "Bearer " + tokenAdmin)).andReturn().getResponse().getStatus();
            assertTrue(status == 400 || status == 404, proibido + ": " + status);
        }
    }

    @Test
    @DisplayName("Seed por endpoint vem desligado, porque apaga o log inteiro")
    void seedPorEndpointDesligado() throws Exception {
        mvc.perform(post("/api/admin/experimento/seed").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------

    private JsonNode verificar() throws Exception {
        MvcResult r = mvc.perform(get("/api/admin/auditoria/verificar-integridade")
                .header("Authorization", "Bearer " + tokenAdmin)).andExpect(status().isOk()).andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }

    private String baixar(String url) throws Exception {
        MvcResult r = mvc.perform(get(url).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk()).andReturn();
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private long contar(String tipo, Long usuario) {
        if (usuario == null) {
            return jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento = ?", Long.class, tipo);
        }
        return jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento = ? AND usuario_id = ?",
                Long.class, tipo, usuario);
    }

    private void restaurar(Map<String, Object> o) {
        jdbc.update("UPDATE log_seguranca SET timestamp = ?, tipo_evento = ?, usuario_id = ?, recurso = ?, ip = ?, "
                        + "resultado = ?, detalhe = ?, hash_anterior = ?, hash_atual = ? WHERE log_id = ?",
                o.get("TIMESTAMP"), o.get("TIPO_EVENTO"), o.get("USUARIO_ID"), o.get("RECURSO"), o.get("IP"),
                o.get("RESULTADO"), texto(o.get("DETALHE")), o.get("HASH_ANTERIOR"), o.get("HASH_ATUAL"), o.get("LOG_ID"));
    }

    /** detalhe e TEXT (CLOB no H2): o queryForMap pode devolver Clob em vez de String. */
    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof java.sql.Clob clob) {
            try {
                return clob.getSubString(1, (int) clob.length());
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
        }
        return valor.toString();
    }
}
