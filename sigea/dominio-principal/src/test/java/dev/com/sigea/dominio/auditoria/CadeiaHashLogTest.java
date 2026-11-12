package dev.com.sigea.dominio.auditoria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class CadeiaHashLogTest {

    @Test
    @DisplayName("Vetor conhecido: hash calculado por implementacao independente (Python hashlib)")
    void vetorConhecido() {
        // Valor obtido fora do Java, com struct.pack('>i', len) + UTF-8 por
        // campo e -1 para nulo, sobre estes mesmos campos. Pega qualquer
        // desvio do formato documentado em CadeiaHashLog.
        String hash = CadeiaHashLog.calcular(42L, LocalDateTime.of(2026, 10, 5, 13, 45, 7, 123_000_000),
                "LOGIN_FALHA", null, "/api/auth/login", "203.0.113.7", "FALHA", "{\"obs\":\"ação\"}",
                CadeiaHashLog.HASH_GENESE);
        assertEquals("ba63f7cc3709208444ffccff850b942809f3475e9c7f00b69a833f19f5736c83", hash);
    }

    @Test
    @DisplayName("Genese sao 64 zeros e o hash sai em hexa minusculo de 64 caracteres")
    void geneseEFormato() {
        assertEquals(64, CadeiaHashLog.HASH_GENESE.length());
        assertTrue(CadeiaHashLog.HASH_GENESE.chars().allMatch(c -> c == '0'));
        String h = cadeia(1).get(0).hashAtual();
        assertTrue(h.matches("[0-9a-f]{64}"), h);
    }

    @Test
    @DisplayName("Serializacao sem ambiguidade: mover caracteres entre campos ou trocar nulo por vazio muda o hash")
    void semAmbiguidade() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 5, 10, 0);
        String a = CadeiaHashLog.calcular(1L, t, "LOGIN_FALHA", 1L, "ab", "c", "FALHA", null, CadeiaHashLog.HASH_GENESE);
        String b = CadeiaHashLog.calcular(1L, t, "LOGIN_FALHA", 1L, "a", "bc", "FALHA", null, CadeiaHashLog.HASH_GENESE);
        String nulo = CadeiaHashLog.calcular(1L, t, "LOGIN_FALHA", null, "a", "bc", "FALHA", null, CadeiaHashLog.HASH_GENESE);
        String vazio = CadeiaHashLog.calcular(1L, t, "LOGIN_FALHA", null, "a", "bc", "FALHA", "", CadeiaHashLog.HASH_GENESE);
        assertNotEquals(a, b);
        assertNotEquals(nulo, vazio);
        // Um microssegundo a mais no timestamp tambem muda: o formato tem 9 casas fixas.
        assertNotEquals(a, CadeiaHashLog.calcular(1L, t.plusNanos(1_000), "LOGIN_FALHA", 1L, "ab", "c", "FALHA", null,
                CadeiaHashLog.HASH_GENESE));
    }

    @Test
    @DisplayName("Cadeia integra passa e devolve o ultimo hash como ancora")
    void cadeiaIntegra() {
        List<RegistroLogSeguranca> cadeia = cadeia(50);
        VerificadorCadeiaLog.Resultado r = verificar(cadeia);
        assertTrue(r.integro());
        assertEquals(50, r.registrosVerificados());
        assertEquals(cadeia.get(49).hashAtual(), r.hashFinal());
        assertEquals(50L, r.ultimoLogIdIntegro());
        assertTrue(verificar(List.of()).integro(), "tabela vazia e integra");
    }

    @Test
    @DisplayName("Editar qualquer coluna de um registro aponta exatamente aquele log_id (inclusive ip e detalhe)")
    void edicaoDeCadaColunaApontaOProprioRegistro() {
        List<UnaryOperator<RegistroLogSeguranca>> edicoes = List.of(
                r -> com(r, r.logId(), r.timestamp().plusNanos(1_000), r.tipoEvento(), r.usuarioId(), r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), "LOGIN_SUCESSO", r.usuarioId(), r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), 999L, r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId() == null ? 7L : null, r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), "/api/outro", r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), r.recurso(), "10.0.0.1", r.resultado(), r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), r.recurso(), r.ip(), "ALERTA", r.detalhe(), r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), r.recurso(), r.ip(), r.resultado(), "{\"n\":2}", r.hashAnterior(), r.hashAtual()),
                r -> com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), r.recurso(), r.ip(), r.resultado(), r.detalhe(), r.hashAnterior(), "f" + r.hashAtual().substring(1)));
        for (int alvo : new int[]{0, 17, 49}) {
            for (int e = 0; e < edicoes.size(); e++) {
                List<RegistroLogSeguranca> cadeia = new ArrayList<>(cadeia(50));
                cadeia.set(alvo, edicoes.get(e).apply(cadeia.get(alvo)));
                VerificadorCadeiaLog.Resultado r = verificar(cadeia);
                assertFalse(r.integro(), "edicao " + e + " no registro " + (alvo + 1));
                assertEquals(alvo + 1L, r.logIdCorrompido(), "edicao " + e);
                assertEquals(VerificadorCadeiaLog.Motivo.HASH_DIVERGENTE, r.motivo(), "edicao " + e);
                assertEquals(alvo == 0 ? null : (long) alvo, r.ultimoLogIdIntegro());
            }
        }
    }

    @Test
    @DisplayName("Editar hash_anterior aponta o proprio registro como encadeamento quebrado")
    void edicaoDoHashAnterior() {
        List<RegistroLogSeguranca> cadeia = new ArrayList<>(cadeia(10));
        RegistroLogSeguranca r = cadeia.get(4);
        cadeia.set(4, com(r, r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), r.recurso(), r.ip(), r.resultado(),
                r.detalhe(), "a".repeat(64), r.hashAtual()));
        VerificadorCadeiaLog.Resultado v = verificar(cadeia);
        assertEquals(5L, v.logIdCorrompido());
        assertEquals(VerificadorCadeiaLog.Motivo.ENCADEAMENTO_QUEBRADO, v.motivo());
    }

    @Test
    @DisplayName("Apagar um registro do meio aponta o seguinte, e o ultimo integro mostra o buraco")
    void registroApagado() {
        List<RegistroLogSeguranca> cadeia = new ArrayList<>(cadeia(10));
        cadeia.remove(5); // log_id 6
        VerificadorCadeiaLog.Resultado v = verificar(cadeia);
        assertEquals(7L, v.logIdCorrompido());
        assertEquals(5L, v.ultimoLogIdIntegro());
        assertEquals(VerificadorCadeiaLog.Motivo.ENCADEAMENTO_QUEBRADO, v.motivo());
    }

    @Test
    @DisplayName("Trocar a ordem de dois registros e detectado")
    void ordemTrocada() {
        List<RegistroLogSeguranca> cadeia = new ArrayList<>(cadeia(10));
        RegistroLogSeguranca a = cadeia.get(3);
        cadeia.set(3, cadeia.get(4));
        cadeia.set(4, a);
        assertEquals(5L, verificar(cadeia).logIdCorrompido());
    }

    @Test
    @DisplayName("Limite documentado: truncar a cauda ou recalcular a cadeia inteira NAO e detectado sem ancora externa")
    void limitesSemAncora() {
        List<RegistroLogSeguranca> cadeia = cadeia(10);
        VerificadorCadeiaLog.Resultado truncada = verificar(cadeia.subList(0, 7));
        assertTrue(truncada.integro());
        assertNotEquals(verificar(cadeia).hashFinal(), truncada.hashFinal(), "so a ancora (hashFinal) denuncia");

        // Atacante com escrita no banco: muda o registro 3 e refaz todos os hashes dali em diante.
        List<RegistroLogSeguranca> refeita = new ArrayList<>();
        String anterior = CadeiaHashLog.HASH_GENESE;
        for (RegistroLogSeguranca r : cadeia) {
            String recurso = r.logId() == 3L ? "/api/forjado" : r.recurso();
            String h = CadeiaHashLog.calcular(r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), recurso, r.ip(),
                    r.resultado(), r.detalhe(), anterior);
            refeita.add(new RegistroLogSeguranca(r.logId(), r.timestamp(), r.tipoEvento(), r.usuarioId(), recurso, r.ip(),
                    r.resultado(), r.detalhe(), anterior, h));
            anterior = h;
        }
        assertTrue(verificar(refeita).integro(), "SHA-256 sem chave nao impede o recalculo");
        assertNotEquals(verificar(cadeia).hashFinal(), verificar(refeita).hashFinal());
    }

    @Test
    @DisplayName("Corte no limite da coluna nao parte par substituto")
    void ajustar() {
        assertNull(CadeiaHashLog.ajustar(null, 5));
        assertEquals("abc", CadeiaHashLog.ajustar("abc", 5));
        assertEquals("abcde", CadeiaHashLog.ajustar("abcdefg", 5));
        String emoji = "abcd😀"; // 4 letras + 1 emoji (2 chars)
        assertEquals("abcd", CadeiaHashLog.ajustar(emoji, 5));
    }

    // ------------------------------------------------------------------

    static List<RegistroLogSeguranca> cadeia(int n) {
        List<RegistroLogSeguranca> lista = new ArrayList<>();
        String anterior = CadeiaHashLog.HASH_GENESE;
        LocalDateTime t = LocalDateTime.of(2026, 10, 5, 12, 0);
        for (int i = 1; i <= n; i++) {
            LocalDateTime ti = t.plusSeconds(i).plusNanos(i * 1_000_000L);
            String tipo = i % 3 == 0 ? "LOGIN_FALHA" : "DECIFRAGEM_OK";
            Long usuario = i % 5 == 0 ? null : 100_000L + i;
            String recurso = "/api/avisos/" + i;
            String ip = "192.0.2." + i;
            String resultado = i % 3 == 0 ? "FALHA" : "SUCESSO";
            String detalhe = "{\"n\":" + i + "}";
            String h = CadeiaHashLog.calcular((long) i, ti, tipo, usuario, recurso, ip, resultado, detalhe, anterior);
            lista.add(new RegistroLogSeguranca((long) i, ti, tipo, usuario, recurso, ip, resultado, detalhe, anterior, h));
            anterior = h;
        }
        return lista;
    }

    private static VerificadorCadeiaLog.Resultado verificar(List<RegistroLogSeguranca> cadeia) {
        VerificadorCadeiaLog v = new VerificadorCadeiaLog();
        cadeia.forEach(v::aceitar);
        return v.resultado();
    }

    private static RegistroLogSeguranca com(RegistroLogSeguranca base, Long id, LocalDateTime t, String tipo, Long usuario,
                                            String recurso, String ip, String resultado, String detalhe,
                                            String anterior, String atual) {
        assertNotNull(base);
        return new RegistroLogSeguranca(id, t, tipo, usuario, recurso, ip, resultado, detalhe, anterior, atual);
    }

    @Test
    @DisplayName("Todos os tipos de evento do brief existem e cabem na coluna")
    void tiposDoBrief() {
        assertEquals(List.of("CIFRAGEM_OK", "CIFRAGEM_FALHA", "DECIFRAGEM_OK", "DECIFRAGEM_FALHA", "LOGIN_SUCESSO",
                        "LOGIN_FALHA", "ESTEGO_EMBUTIR", "ESTEGO_EXTRAIR", "ATAQUE_EXECUTADO", "ANOMALIA_DETECTADA",
                        "INTEGRIDADE_VIOLADA"),
                Arrays.stream(TipoEventoSeguranca.values()).map(Enum::name).toList());
        for (TipoEventoSeguranca t : TipoEventoSeguranca.values()) {
            assertTrue(t.name().length() <= CadeiaHashLog.TAMANHO_TIPO_EVENTO);
        }
    }
}
