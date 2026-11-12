package dev.com.sigea.infraestrutura.auditoria;

import dev.com.sigea.dominio.auditoria.CadeiaHashLog;
import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.experimento.EventoExperimental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unico caminho de escrita em log_seguranca.
 *
 * A cadeia exige ordem total: cada linha depende do hash da anterior. Por
 * isso toda gravacao passa por uma trava unica - ler a cabeca (ultimo
 * log_id e hash_atual), calcular e inserir acontecem sem ninguem no meio.
 * A trava e da JVM: com mais de uma instancia do app, a segunda a gravar o
 * mesmo log_id falha na PK, e o evento se perde em vez de a cadeia
 * bifurcar em silencio. O SIGEA roda numa instancia so no Railway; com
 * mais, a trava teria de ir para o banco (SELECT ... FOR UPDATE numa linha
 * de controle).
 *
 * JDBC e nao JPA: o seeder grava ate 500 mil linhas, e com JPA e id
 * IDENTITY o Hibernate desliga o batch de INSERT. Aqui o batch e explicito.
 *
 * O texto e cortado no limite de cada coluna ANTES do hash: se o banco
 * cortasse depois (ou recusasse), o hash gravado nao corresponderia a linha
 * gravada e a verificacao acusaria corrupcao numa linha integra.
 */
@Service
public class RegistradorLogSeguranca {

    static final String INSERT = "INSERT INTO log_seguranca (log_id, timestamp, tipo_evento, usuario_id, recurso, ip, "
            + "resultado, detalhe, hash_anterior, hash_atual) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ROTULO =
            "INSERT INTO rotulo_evento_experimental (log_id, incidente_id, cenario) VALUES (?, ?, ?)";
    private static final int TAMANHO_LOTE = 2_000;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transacao;
    private final Clock relogio;
    private final ReentrantLock trava = new ReentrantLock(true);

    @Autowired
    public RegistradorLogSeguranca(DataSource dataSource, PlatformTransactionManager transacoes) {
        this(dataSource, transacoes, Clock.systemUTC());
    }

    RegistradorLogSeguranca(DataSource dataSource, PlatformTransactionManager transacoes, Clock relogio) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.transacao = new TransactionTemplate(transacoes);
        this.relogio = relogio;
    }

    /** @return o log_id atribuido. */
    public long registrar(NovoEventoSeguranca evento) {
        return registrarTodos(List.of(evento)).get(0);
    }

    /** Grava em ordem, numa transacao so; todos recebem o mesmo timestamp. */
    public List<Long> registrarTodos(List<NovoEventoSeguranca> eventos) {
        if (eventos.isEmpty()) {
            return List.of();
        }
        // Milissegundos: o TIMESTAMP do H2 guarda ate microssegundos, e o
        // nanossegundo do relogio do Windows nao voltaria igual do banco.
        LocalDateTime agora = LocalDateTime.now(relogio).truncatedTo(ChronoUnit.MILLIS);
        trava.lock();
        try {
            return transacao.execute(status -> {
                Linha cabeca = cabeca();
                long id = cabeca == null ? 0 : cabeca.logId;
                String anterior = cabeca == null ? CadeiaHashLog.HASH_GENESE : cabeca.hashAtual;
                List<Linha> linhas = new ArrayList<>(eventos.size());
                for (NovoEventoSeguranca e : eventos) {
                    Linha l = Linha.de(++id, agora, e, anterior);
                    linhas.add(l);
                    anterior = l.hashAtual;
                }
                inserir(linhas);
                return linhas.stream().map(l -> l.logId).toList();
            });
        } finally {
            trava.unlock();
        }
    }

    public record ResultadoGravacao(long eventos, long rotulos) {
    }

    /**
     * Apaga o log inteiro e grava o volume experimental com log_id de 1 a n.
     *
     * Apagar e de proposito: o banco do Railway e recriado a cada deploy e o
     * volume tem de ser regeneravel, nunca acumulado (regra do brief). Como
     * a numeracao recomeca em 1, a mesma semente produz a mesma tabela,
     * hash por hash. Eventos reais anteriores (logins) se perdem - por isso
     * o endpoint que dispara isto vem desligado por padrao.
     */
    public ResultadoGravacao substituirPorVolumeExperimental(List<EventoExperimental> eventos) {
        trava.lock();
        try {
            return transacao.execute(status -> {
                jdbc.update("DELETE FROM rotulo_evento_experimental");
                jdbc.update("DELETE FROM log_seguranca");
                String anterior = CadeiaHashLog.HASH_GENESE;
                List<Linha> lote = new ArrayList<>(TAMANHO_LOTE);
                List<Object[]> rotulos = new ArrayList<>();
                long id = 0;
                for (EventoExperimental e : eventos) {
                    Linha l = Linha.de(++id, e.timestamp(), e.comoNovoEvento(), anterior);
                    anterior = l.hashAtual;
                    lote.add(l);
                    if (e.cenario() != null) {
                        rotulos.add(new Object[]{id, e.incidente(), e.cenario().name()});
                    }
                    if (lote.size() == TAMANHO_LOTE) {
                        inserir(lote);
                        lote.clear();
                    }
                }
                inserir(lote);
                jdbc.batchUpdate(INSERT_ROTULO, rotulos, TAMANHO_LOTE, (ps, r) -> {
                    ps.setLong(1, (Long) r[0]);
                    ps.setInt(2, (Integer) r[1]);
                    ps.setString(3, (String) r[2]);
                });
                return new ResultadoGravacao(id, rotulos.size());
            });
        } finally {
            trava.unlock();
        }
    }

    private Linha cabeca() {
        List<Linha> r = jdbc.query("SELECT log_id, hash_atual FROM log_seguranca ORDER BY log_id DESC FETCH FIRST 1 ROWS ONLY",
                (rs, n) -> new Linha(rs.getLong(1), null, null, null, null, null, null, null, null, rs.getString(2)));
        return r.isEmpty() ? null : r.get(0);
    }

    private void inserir(List<Linha> linhas) {
        if (linhas.isEmpty()) {
            return;
        }
        jdbc.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Linha l = linhas.get(i);
                ps.setLong(1, l.logId);
                ps.setObject(2, l.timestamp);
                ps.setString(3, l.tipoEvento);
                if (l.usuarioId == null) {
                    ps.setNull(4, Types.BIGINT);
                } else {
                    ps.setLong(4, l.usuarioId);
                }
                ps.setString(5, l.recurso);
                ps.setString(6, l.ip);
                ps.setString(7, l.resultado);
                ps.setString(8, l.detalhe);
                ps.setString(9, l.hashAnterior);
                ps.setString(10, l.hashAtual);
            }

            @Override
            public int getBatchSize() {
                return linhas.size();
            }
        });
    }

    private record Linha(long logId, LocalDateTime timestamp, String tipoEvento, Long usuarioId, String recurso,
                         String ip, String resultado, String detalhe, String hashAnterior, String hashAtual) {

        static Linha de(long logId, LocalDateTime timestamp, NovoEventoSeguranca e, String hashAnterior) {
            String tipo = CadeiaHashLog.ajustar(e.tipo().name(), CadeiaHashLog.TAMANHO_TIPO_EVENTO);
            String recurso = CadeiaHashLog.ajustar(e.recurso(), CadeiaHashLog.TAMANHO_RECURSO);
            String ip = CadeiaHashLog.ajustar(e.ip(), CadeiaHashLog.TAMANHO_IP);
            String resultado = CadeiaHashLog.ajustar(e.resultado().name(), CadeiaHashLog.TAMANHO_RESULTADO);
            String hash = CadeiaHashLog.calcular(logId, timestamp, tipo, e.usuarioId(), recurso, ip, resultado,
                    e.detalhe(), hashAnterior);
            return new Linha(logId, timestamp, tipo, e.usuarioId(), recurso, ip, resultado, e.detalhe(), hashAnterior, hash);
        }
    }
}
