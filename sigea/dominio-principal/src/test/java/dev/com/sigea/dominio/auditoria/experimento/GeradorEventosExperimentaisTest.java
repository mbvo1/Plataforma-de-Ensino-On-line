package dev.com.sigea.dominio.auditoria.experimento;

import dev.com.sigea.dominio.auditoria.CadeiaHashLog;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais.Cenario;
import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais.Incidente;
import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais.Lote;
import dev.com.sigea.dominio.cripto.Evidencias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 4 - o volume sintetico, medido sem banco. Os CSVs descrevem o volume
 * padrao (QUANTIDADE_PADRAO, SEMENTE_PADRAO), que e exatamente o que o
 * perfil seed-eventos grava no app; por isso a Fase 5 pode citar estes
 * numeros como a composicao do conjunto que o detector recebeu.
 */
class GeradorEventosExperimentaisTest {

    /** LocalDateTime.toString omite segundos zerados; formato fixo para o CSV ser uniforme. */
    private static final java.time.format.DateTimeFormatter DATA =
            java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");

    @Test
    @DisplayName("Mesma quantidade e semente geram exatamente o mesmo volume; semente diferente gera outro")
    void deterministico() {
        Lote a = GeradorEventosExperimentais.gerar(20_000, 7);
        Lote b = GeradorEventosExperimentais.gerar(20_000, 7);
        Lote c = GeradorEventosExperimentais.gerar(20_000, 8);
        assertEquals(assinatura(a.eventos()), assinatura(b.eventos()));
        assertEquals(a.incidentes(), b.incidentes());
        assertNotEquals(assinatura(a.eventos()), assinatura(c.eventos()));
    }

    @Test
    @DisplayName("Quantidade exata, ordem temporal, campos dentro dos limites das colunas, sem vazar o rotulo")
    void formaDoVolume() {
        Lote lote = GeradorEventosExperimentais.gerar(30_000, 11);
        List<EventoExperimental> ev = lote.eventos();
        assertEquals(30_000, ev.size());
        for (int i = 1; i < ev.size(); i++) {
            assertTrue(ev.get(i - 1).instanteMillis <= ev.get(i).instanteMillis, "fora de ordem em " + i);
        }
        Set<String> nomesDeCenario = new HashSet<>();
        for (Cenario c : Cenario.values()) {
            nomesDeCenario.add(c.name());
        }
        for (EventoExperimental e : ev) {
            assertTrue(e.recurso().length() <= CadeiaHashLog.TAMANHO_RECURSO);
            assertTrue(e.ip().length() <= CadeiaHashLog.TAMANHO_IP);
            assertNotEquals(TipoEventoSeguranca.ANOMALIA_DETECTADA, e.tipo(), "ANOMALIA_DETECTADA e saida do detector, nao entrada");
            assertTrue(e.usuarioId() >= GeradorEventosExperimentais.PRIMEIRO_USUARIO);
            for (String nome : nomesDeCenario) {
                assertFalse(e.detalhe().contains(nome) || e.recurso().contains(nome), "rotulo vazou no evento");
            }
            assertEquals(e.cenario() == null, e.incidente() < 0);
        }
    }

    @Test
    @DisplayName("Todo cenario injetado, cada incidente com seus eventos rotulados e contados")
    void incidentes() {
        Lote lote = GeradorEventosExperimentais.gerar(30_000, 11);
        assertEquals(5 * 3, lote.incidentes().size(), "3 de cada cenario em 30 mil eventos");
        Map<Integer, Integer> porIncidente = new HashMap<>();
        for (EventoExperimental e : lote.eventos()) {
            if (e.incidente() >= 0) {
                porIncidente.merge(e.incidente(), 1, Integer::sum);
            }
        }
        for (Incidente inc : lote.incidentes()) {
            assertEquals(inc.eventos(), porIncidente.get(inc.id()), "incidente " + inc.id());
            assertFalse(inc.fim().isBefore(inc.inicio()));
        }
        assertThrows(IllegalArgumentException.class, () -> GeradorEventosExperimentais.gerar(500, 1),
                "volume em que os incidentes passam de metade nao e aceito");
    }

    @Test
    @DisplayName("Evidencia: composicao do volume padrao do seeder (100 mil eventos)")
    void composicaoDoVolumePadrao() {
        long inicio = System.nanoTime();
        Lote lote = GeradorEventosExperimentais.gerar(GeradorEventosExperimentais.QUANTIDADE_PADRAO,
                GeradorEventosExperimentais.SEMENTE_PADRAO);
        double segundos = (System.nanoTime() - inicio) / 1e9;
        System.out.printf("[Seeder] geracao em memoria de %d eventos: %.2f s (nao vai para CSV: depende da maquina)%n",
                lote.eventos().size(), segundos);

        Map<String, long[]> porTipo = new java.util.TreeMap<>();
        long[][] porHora = new long[24][2];
        long[][] porDiaSemana = new long[7][2];
        Map<Cenario, long[]> porCenario = new EnumMap<>(Cenario.class);
        Set<Long> usuarios = new HashSet<>();
        for (EventoExperimental e : lote.eventos()) {
            int anomalo = e.cenario() == null ? 0 : 1;
            porTipo.computeIfAbsent(e.tipo().name() + "," + e.resultado().name(), k -> new long[2])[anomalo]++;
            LocalDateTime local = e.timestamp().atOffset(ZoneOffset.UTC)
                    .withOffsetSameInstant(GeradorEventosExperimentais.FUSO_AULAS).toLocalDateTime();
            porHora[local.getHour()][anomalo]++;
            porDiaSemana[local.getDayOfWeek().getValue() - 1][anomalo]++;
            if (e.cenario() != null) {
                porCenario.computeIfAbsent(e.cenario(), k -> new long[1])[0]++;
            }
            usuarios.add(e.usuarioId());
        }

        Evidencias.Csv tipos = new Evidencias.Csv("seeder-tipos.csv", "tipo_evento,resultado,eventos_normais,eventos_anomalos");
        porTipo.forEach((k, v) -> tipos.linha("%s,%d,%d", k, v[0], v[1]));
        tipos.gravar();

        Evidencias.Csv horas = new Evidencias.Csv("seeder-horas.csv", "hora_local_recife,eventos_normais,eventos_anomalos");
        for (int h = 0; h < 24; h++) {
            horas.linha("%d,%d,%d", h, porHora[h][0], porHora[h][1]);
        }
        horas.gravar();

        Evidencias.Csv dias = new Evidencias.Csv("seeder-dias-semana.csv", "dia_semana,eventos_normais,eventos_anomalos");
        String[] nomes = {"segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo"};
        for (int d = 0; d < 7; d++) {
            dias.linha("%s,%d,%d", nomes[d], porDiaSemana[d][0], porDiaSemana[d][1]);
        }
        dias.gravar();

        Evidencias.Csv incidentes = new Evidencias.Csv("seeder-incidentes.csv",
                "incidente_id,cenario,usuario_id,inicio_utc,fim_utc,duracao_s,eventos");
        for (Incidente i : lote.incidentes()) {
            incidentes.linha("%d,%s,%d,%s,%s,%d,%d", i.id(), i.cenario(), i.usuarioId(), DATA.format(i.inicio()), DATA.format(i.fim()),
                    java.time.Duration.between(i.inicio(), i.fim()).toSeconds(), i.eventos());
        }
        incidentes.gravar();

        long anomalos = porCenario.values().stream().mapToLong(v -> v[0]).sum();
        Evidencias.Csv resumo = new Evidencias.Csv("seeder-resumo.csv", "metrica,valor");
        resumo.linha("quantidade,%d", lote.eventos().size());
        resumo.linha("semente,%d", GeradorEventosExperimentais.SEMENTE_PADRAO);
        resumo.linha("periodo_inicio_local,%s", GeradorEventosExperimentais.INICIO);
        resumo.linha("periodo_dias,%d", GeradorEventosExperimentais.DIAS);
        resumo.linha("usuarios_sinteticos,%d", lote.usuarios());
        resumo.linha("usuarios_com_eventos,%d", usuarios.size());
        resumo.linha("turmas,%d", lote.turmas());
        resumo.linha("incidentes,%d", lote.incidentes().size());
        resumo.linha("eventos_rotulados_anomalos,%d", anomalos);
        resumo.linha("fracao_anomala_pct,%.4f", 100.0 * anomalos / lote.eventos().size());
        for (Cenario c : Cenario.values()) {
            resumo.linha("eventos_%s,%d", c.name().toLowerCase(), porCenario.getOrDefault(c, new long[1])[0]);
        }
        long sucessos = lote.eventos().stream().filter(e -> e.resultado() == ResultadoEvento.SUCESSO).count();
        resumo.linha("sucesso_pct,%.4f", 100.0 * sucessos / lote.eventos().size());
        resumo.gravar();

        assertEquals(GeradorEventosExperimentais.QUANTIDADE_PADRAO, lote.eventos().size());
        assertEquals(5 * 10, lote.incidentes().size());
        assertEquals(Cenario.values().length, porCenario.size());
    }

    private static String assinatura(List<EventoExperimental> eventos) {
        StringBuilder sb = new StringBuilder();
        for (EventoExperimental e : eventos) {
            sb.append(e.instanteMillis).append(e.tipo()).append(e.usuarioId()).append(e.recurso()).append(e.ip())
                    .append(e.resultado()).append(e.cenario()).append(e.incidente()).append('\n');
        }
        return sb.toString();
    }
}
