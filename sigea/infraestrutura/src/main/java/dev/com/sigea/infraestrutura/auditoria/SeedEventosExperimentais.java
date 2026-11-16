package dev.com.sigea.infraestrutura.auditoria;

import dev.com.sigea.dominio.auditoria.experimento.GeradorEventosExperimentais;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Seeder do volume experimental da Fase 4 (100 mil a 500 mil eventos).
 *
 * Duas formas de disparar, como pede o brief:
 *   - perfil Spring seed-eventos: grava na inicializacao. E o caminho do
 *     Railway, onde o banco nasce vazio a cada deploy;
 *   - POST /api/admin/experimento/seed, so para ADMINISTRADOR e so se
 *     sigea.experimento.seed.endpoint-habilitado=true. Vem desligado porque
 *     o seeder APAGA o log de seguranca inteiro antes de gravar: um token de
 *     admin roubado nao deve bastar para limpar a trilha de auditoria.
 *
 * A geracao fica no dominio (GeradorEventosExperimentais, so JDK); aqui so
 * se valida o tamanho, grava e cronometra.
 */
@Service
public class SeedEventosExperimentais {

    public static final int MINIMO = 100_000;
    public static final int MAXIMO = 500_000;

    private static final Logger LOG = LoggerFactory.getLogger(SeedEventosExperimentais.class);

    public record Resultado(int quantidade, long semente, long eventosGravados, long rotulosGravados, int incidentes,
                            long milissegundosGeracao, long milissegundosGravacao) {
    }

    private final RegistradorLogSeguranca registrador;

    public SeedEventosExperimentais(RegistradorLogSeguranca registrador) {
        this.registrador = registrador;
    }

    public Resultado executar(int quantidade, long semente) {
        if (quantidade < MINIMO || quantidade > MAXIMO) {
            throw new IllegalArgumentException("quantidade deve ficar entre " + MINIMO + " e " + MAXIMO + ": " + quantidade);
        }
        long t0 = System.nanoTime();
        GeradorEventosExperimentais.Lote lote = GeradorEventosExperimentais.gerar(quantidade, semente);
        long t1 = System.nanoTime();
        RegistradorLogSeguranca.ResultadoGravacao gravado = registrador.substituirPorVolumeExperimental(lote.eventos());
        long t2 = System.nanoTime();
        Resultado r = new Resultado(quantidade, semente, gravado.eventos(), gravado.rotulos(), lote.incidentes().size(),
                (t1 - t0) / 1_000_000, (t2 - t1) / 1_000_000);
        LOG.info("Seeder: {} eventos, {} rotulados em {} incidentes; geracao {} ms, gravacao {} ms",
                r.eventosGravados(), r.rotulosGravados(), r.incidentes(), r.milissegundosGeracao(), r.milissegundosGravacao());
        return r;
    }

    /** Perfil seed-eventos: regrava o volume na subida do app. */
    @Component
    @Profile("seed-eventos")
    static class NaInicializacao implements ApplicationRunner {

        private final SeedEventosExperimentais seeder;
        private final int quantidade;
        private final long semente;

        NaInicializacao(SeedEventosExperimentais seeder,
                        @Value("${sigea.experimento.seed.quantidade:" + GeradorEventosExperimentais.QUANTIDADE_PADRAO + "}") int quantidade,
                        @Value("${sigea.experimento.seed.semente:" + GeradorEventosExperimentais.SEMENTE_PADRAO + "}") long semente) {
            this.seeder = seeder;
            this.quantidade = quantidade;
            this.semente = semente;
        }

        @Override
        public void run(ApplicationArguments args) {
            seeder.executar(quantidade, semente);
        }
    }
}
