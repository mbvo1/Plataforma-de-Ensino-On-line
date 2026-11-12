package dev.com.sigea.infraestrutura.auditoria;

import dev.com.sigea.dominio.auditoria.RegistroLogSeguranca;
import dev.com.sigea.dominio.auditoria.VerificadorCadeiaLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Leitura de log_seguranca em fluxo: verificacao da cadeia e exportacao
 * para a Fase 5. Nenhum dos dois carrega a tabela na memoria - com 500 mil
 * linhas isso custaria centenas de MB. Cada linha e entregue ao consumidor
 * assim que sai do ResultSet.
 */
@Service
public class ConsultaLogSeguranca {

    private static final String COLUNAS = "log_id, timestamp, tipo_evento, usuario_id, recurso, ip, resultado, detalhe, "
            + "hash_anterior, hash_atual";

    public record Rotulo(long logId, int incidenteId, String cenario, Long usuarioId, LocalDateTime timestamp) {
    }

    private final JdbcTemplate jdbc;

    public ConsultaLogSeguranca(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.jdbc.setFetchSize(1_000);
    }

    /**
     * Percorre a cadeia inteira em ordem de log_id. Nao toma a trava do
     * registrador: uma linha gravada durante a leitura entra ou nao, mas
     * cada linha lida e conferida contra a anterior lida, entao o resultado
     * vale para o trecho percorrido.
     */
    public VerificadorCadeiaLog.Resultado verificarIntegridade() {
        VerificadorCadeiaLog verificador = new VerificadorCadeiaLog();
        jdbc.query("SELECT " + COLUNAS + " FROM log_seguranca ORDER BY log_id", rs -> {
            // Sem como interromper o RowCallbackHandler; depois da primeira
            // violacao o verificador so ignora o resto.
            verificador.aceitar(registro(rs));
        });
        return verificador.resultado();
    }

    /** Eventos com desde <= timestamp < ate, em ordem de log_id; limites nulos = sem limite. */
    public void exportar(LocalDateTime desde, LocalDateTime ate, Consumer<RegistroLogSeguranca> consumidor) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUNAS + " FROM log_seguranca WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            sql.append(" AND timestamp >= ?");
            parametros.add(desde);
        }
        if (ate != null) {
            sql.append(" AND timestamp < ?");
            parametros.add(ate);
        }
        sql.append(" ORDER BY log_id");
        jdbc.query(sql.toString(), rs -> {
            consumidor.accept(registro(rs));
        }, parametros.toArray());
    }

    public void rotulos(Consumer<Rotulo> consumidor) {
        jdbc.query("SELECT r.log_id, r.incidente_id, r.cenario, l.usuario_id, l.timestamp "
                + "FROM rotulo_evento_experimental r JOIN log_seguranca l ON l.log_id = r.log_id ORDER BY r.log_id", rs -> {
            long usuario = rs.getLong(4);
            consumidor.accept(new Rotulo(rs.getLong(1), rs.getInt(2), rs.getString(3), rs.wasNull() ? null : usuario,
                    rs.getObject(5, LocalDateTime.class)));
        });
    }

    public long contarEventos() {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca", Long.class);
        return n == null ? 0 : n;
    }

    static RegistroLogSeguranca registro(ResultSet rs) throws SQLException {
        long usuario = rs.getLong("usuario_id");
        Long usuarioId = rs.wasNull() ? null : usuario;
        return new RegistroLogSeguranca(rs.getLong("log_id"), rs.getObject("timestamp", LocalDateTime.class),
                rs.getString("tipo_evento"), usuarioId, rs.getString("recurso"), rs.getString("ip"),
                rs.getString("resultado"), rs.getString("detalhe"), rs.getString("hash_anterior"),
                rs.getString("hash_atual"));
    }
}
