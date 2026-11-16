package dev.com.sigea.infraestrutura.persistencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Cifra, na subida do app, o conteudo de avisos e topicos que ainda esta em
 * texto claro (linhas gravadas antes da CifraSigea entrar nos converters).
 *
 * Runner e nao migration Flyway: a migration rodaria uma vez so, e a
 * cifragem precisa da chave, que vem do ambiente e nao do SQL. Assim e
 * idempotente - linha que ja comeca com "v1:" fica como esta - e cobre o
 * banco local antigo, o banco novo do Railway (vazio, nao faz nada) e
 * qualquer linha que alguem inserir em claro pelo console.
 *
 * Sem isso, o legado continuaria legivel (o converter aceita texto sem
 * prefixo), mas so seria cifrado quando alguem editasse o registro.
 */
@Component
public class RecifragemConteudoLegado implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RecifragemConteudoLegado.class);

    private final JdbcTemplate jdbc;
    private final CifraConteudo cifra;

    public RecifragemConteudoLegado(DataSource dataSource, CifraConteudo cifra) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.cifra = cifra;
    }

    @Override
    public void run(ApplicationArguments args) {
        int avisos = recifrar("avisos", "aviso_id", "avisos.conteudo");
        int topicos = recifrar("Topicos", "topico_id", "topicos.conteudo");
        if (avisos + topicos > 0) {
            LOG.info("Conteudo legado cifrado com a CifraSigea: {} avisos, {} topicos", avisos, topicos);
        }
    }

    /** @return quantas linhas foram cifradas agora. */
    int recifrar(String tabela, String coluna, String recurso) {
        // Nomes fixos neste arquivo, nunca vindos de fora: por isso podem ir
        // concatenados no SQL.
        List<Object[]> emClaro = jdbc.query(
                "SELECT " + coluna + ", conteudo FROM " + tabela
                        + " WHERE conteudo IS NOT NULL AND conteudo NOT LIKE 'v1:%'",
                (rs, n) -> new Object[]{rs.getLong(1), rs.getString(2)});
        for (Object[] linha : emClaro) {
            jdbc.update("UPDATE " + tabela + " SET conteudo = ? WHERE " + coluna + " = ?",
                    cifra.cifrar(recurso, (String) linha[1]), linha[0]);
        }
        return emClaro.size();
    }
}
