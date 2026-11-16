package dev.com.sigea.dominio.auditoria.experimento;

import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Volume sintetico para a Fase 5: eventos de uso normal do SIGEA com
 * incidentes injetados, e a verdade conhecida de cada incidente.
 *
 * Sem framework, so JDK, como a cifra: o perfil Spring e o endpoint de admin
 * (SeedEventosExperimentais, na infraestrutura) so chamam este gerador e
 * gravam o resultado. Assim o mesmo volume pode ser gerado e medido num
 * teste de unidade, sem banco.
 *
 * Determinismo: a mesma (quantidade, semente) gera exatamente os mesmos
 * eventos, na mesma ordem, com o mesmo periodo fixo. O banco do Railway e
 * recriado a cada deploy; o volume tem de ser regeneravel, nao acumulado.
 *
 * Todos os parametros abaixo foram fixados ANTES de rodar o detector da
 * Fase 5 e nao devem ser mexidos para o detector parecer melhor. Se um
 * mudar, o motivo vai para o README das evidencias.
 *
 * Modelo de uso normal:
 *   - Duas semanas de aula a partir de uma segunda-feira, horario de
 *     Recife (UTC-3, sem horario de verao desde 2019), gravado em UTC.
 *   - Picos de manha e de tarde em dia util, um pico menor a noite, fim de
 *     semana fraco, madrugada quase vazia mas nao vazia.
 *   - Usuarios 88% alunos, 10% professores, 2% administradores, com nivel
 *     de atividade desigual (lognormal): poucos muito ativos, muitos leves.
 *   - Sessao: login (6% com um erro de senha antes, 1% com dois), depois
 *     acoes cuja mistura depende do perfil, com intervalo medio de 45 s.
 *   - Falhas benignas existem (senha errada, decifragem que falha de vez em
 *     quando): sem elas, qualquer falha seria anomalia e o detector
 *     pareceria perfeito por construcao.
 *
 * Incidentes injetados, um tipo por anomalia do enunciado. A intensidade de
 * cada incidente e sorteada numa faixa que comeca perto do comportamento
 * normal, para o problema nao ser trivial. A excecao assumida e
 * VIOLACAO_INTEGRIDADE: INTEGRIDADE_VIOLADA nunca ocorre em uso normal,
 * entao esse cenario e separavel por construcao - e e assim tambem no
 * sistema real.
 */
public final class GeradorEventosExperimentais {

    public enum Cenario {
        /** Rajada de LOGIN_FALHA contra um usuario. Anomalia: numero incomum de tentativas de autenticacao. */
        FORCA_BRUTA_LOGIN,
        /** Rajada de DECIFRAGEM_FALHA. Anomalia: frequencia elevada de falhas de descriptografia. */
        FALHAS_DECIFRAGEM,
        /** INTEGRIDADE_VIOLADA em arquivos. Anomalia: alteracoes inesperadas em arquivos. */
        VIOLACAO_INTEGRIDADE,
        /** Madrugada, dezenas de recursos fora das turmas do usuario. Anomalia: padroes atipicos de acesso. */
        ACESSO_ATIPICO,
        /** Intervalo quase constante entre eventos, como um robo. Anomalia: geracao incomum de eventos. */
        GERACAO_AUTOMATIZADA
    }

    public record Incidente(int id, Cenario cenario, long usuarioId, LocalDateTime inicio, LocalDateTime fim,
                            int eventos) {
    }

    public record Lote(List<EventoExperimental> eventos, List<Incidente> incidentes, int usuarios, int turmas) {
    }

    /**
     * Padrao do seeder no app e nos CSVs de evidencia: o volume descrito em
     * seeder-*.csv e exatamente o que o perfil seed-eventos grava.
     */
    public static final int QUANTIDADE_PADRAO = 100_000;
    public static final long SEMENTE_PADRAO = 0x5EED_2026L;

    /** Segunda-feira; o periodo e fixo para o volume sair igual em qualquer data de execucao. */
    public static final LocalDate INICIO = LocalDate.of(2026, 10, 5);
    public static final int DIAS = 14;
    public static final ZoneOffset FUSO_AULAS = ZoneOffset.ofHours(-3);

    /** Ids bem acima dos usuarios reais, para nenhum evento sintetico ser atribuido a uma pessoa de verdade. */
    public static final long PRIMEIRO_USUARIO = 100_001;
    public static final int EVENTOS_POR_USUARIO = 160;
    public static final int USUARIOS_POR_TURMA = 30;

    /** Um incidente de cada cenario a cada 10 mil eventos: 10 de cada em 100 mil. */
    public static final int EVENTOS_POR_INCIDENTE_DE_CADA_CENARIO = 10_000;

    static final int PERFIL_ALUNO = 0;
    static final int PERFIL_PROFESSOR = 1;
    static final int PERFIL_ADMIN = 2;

    static final int IPS_DISPONIVEIS = 3 * 254;
    static final int AVISOS_POR_TURMA = 60;

    /** Peso relativo de cada hora local (0 a 23) no inicio de uma sessao. */
    static final double[] PESO_HORA = {
            0.30, 0.15, 0.10, 0.08, 0.08, 0.15, 0.60, 2.50, 4.00, 4.00, 4.00, 3.50,
            2.00, 3.00, 4.00, 4.00, 3.50, 2.50, 2.00, 2.50, 2.50, 2.00, 1.20, 0.60};

    /** Segunda a domingo. */
    static final double[] PESO_DIA_SEMANA = {1.0, 1.0, 1.0, 1.0, 1.0, 0.35, 0.20};

    private final SplittableRandom rnd;
    private final int quantidade;
    private final int[] perfil;
    private final double[] atividadeAcumulada;
    private final int[] ip1;
    private final int[] ip2;
    private final int[][] turmasDoUsuario;
    private final int turmas;
    private final double[] horaAcumulada = acumular(PESO_HORA);
    private final double[] diaAcumulado;
    private final List<EventoExperimental> eventos = new ArrayList<>();
    private final List<Incidente> incidentes = new ArrayList<>();

    private GeradorEventosExperimentais(int quantidade, long semente) {
        this.rnd = new SplittableRandom(semente);
        this.quantidade = quantidade;
        int usuarios = Math.max(50, quantidade / EVENTOS_POR_USUARIO);
        this.turmas = Math.max(5, usuarios / USUARIOS_POR_TURMA);
        this.perfil = new int[usuarios];
        this.ip1 = new int[usuarios];
        this.ip2 = new int[usuarios];
        this.turmasDoUsuario = new int[usuarios][];
        double[] atividade = new double[usuarios];
        for (int u = 0; u < usuarios; u++) {
            // Os dois primeiros garantem pelo menos um admin e um professor em qualquer tamanho.
            double sorteio = rnd.nextDouble();
            perfil[u] = u == 0 ? PERFIL_ADMIN : u == 1 ? PERFIL_PROFESSOR
                    : sorteio < 0.88 ? PERFIL_ALUNO : sorteio < 0.98 ? PERFIL_PROFESSOR : PERFIL_ADMIN;
            atividade[u] = Math.exp(0.8 * gaussiana());
            ip1[u] = rnd.nextInt(IPS_DISPONIVEIS);
            ip2[u] = rnd.nextDouble() < 0.4 ? rnd.nextInt(IPS_DISPONIVEIS) : ip1[u];
            int n = switch (perfil[u]) {
                case PERFIL_ALUNO -> 1 + rnd.nextInt(3);
                case PERFIL_PROFESSOR -> 1 + rnd.nextInt(4);
                default -> 2;
            };
            turmasDoUsuario[u] = rnd.ints(n, 0, turmas).toArray();
        }
        this.atividadeAcumulada = acumular(atividade);
        double[] pesoDia = new double[DIAS];
        for (int d = 0; d < DIAS; d++) {
            pesoDia[d] = PESO_DIA_SEMANA[INICIO.plusDays(d).getDayOfWeek().getValue() - 1];
        }
        this.diaAcumulado = acumular(pesoDia);
    }

    public static Lote gerar(int quantidade, long semente) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("quantidade deve ser positiva: " + quantidade);
        }
        GeradorEventosExperimentais g = new GeradorEventosExperimentais(quantidade, semente);
        g.injetarIncidentes();
        int anomalos = g.eventos.size();
        if (anomalos * 2 > quantidade) {
            throw new IllegalArgumentException("quantidade pequena demais: os incidentes injetados ("
                    + anomalos + " eventos) passariam de metade do volume de " + quantidade);
        }
        while (g.eventos.size() < quantidade) {
            g.sessaoNormal();
        }
        // A ultima sessao pode passar do total; corta so eventos normais, que vem depois dos incidentes na lista.
        List<EventoExperimental> lista = new ArrayList<>(g.eventos.subList(0, quantidade));
        // Ordem da cadeia = ordem do tempo. Sort estavel: empate de milissegundo mantem a ordem de geracao.
        lista.sort(Comparator.comparingLong(e -> e.instanteMillis));
        return new Lote(List.copyOf(lista), List.copyOf(g.incidentes), g.perfil.length, g.turmas);
    }

    // ------------------------------------------------------------------ uso normal

    private void sessaoNormal() {
        int u = sortear(atividadeAcumulada);
        long t = instanteDeAula();
        int ip = rnd.nextDouble() < 0.75 ? ip1[u] : ip2[u];
        double erro = rnd.nextDouble();
        int falhasDeSenha = erro < 0.01 ? 2 : erro < 0.07 ? 1 : 0;
        for (int i = 0; i < falhasDeSenha; i++) {
            evento(t, TipoEventoSeguranca.LOGIN_FALHA, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.FALHA, null, -1);
            t += 1_000 + exponencial(6_000);
        }
        evento(t, TipoEventoSeguranca.LOGIN_SUCESSO, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.SUCESSO, null, -1);
        double mediaAcoes = switch (perfil[u]) {
            case PERFIL_ALUNO -> 8;
            case PERFIL_PROFESSOR -> 12;
            default -> 10;
        };
        int acoes = 1 + (int) exponencial(mediaAcoes);
        for (int i = 0; i < acoes; i++) {
            t += Math.max(1_000, (long) exponencial(45_000));
            acaoNormal(u, t, ip);
        }
    }

    private void acaoNormal(int u, long t, int ip) {
        double r = rnd.nextDouble();
        switch (perfil[u]) {
            case PERFIL_ALUNO -> {
                if (r < 0.85) {
                    leitura(u, t, ip);
                } else if (r < 0.97) {
                    escrita(u, t, ip, EventoExperimental.RECURSO_TOPICO);
                } else {
                    estego(u, t, ip, false);
                }
            }
            case PERFIL_PROFESSOR -> {
                if (r < 0.60) {
                    leitura(u, t, ip);
                } else if (r < 0.90) {
                    escrita(u, t, ip, EventoExperimental.RECURSO_AVISO);
                } else {
                    estego(u, t, ip, r < 0.95);
                }
            }
            default -> {
                if (r < 0.50) {
                    leitura(u, t, ip);
                } else if (r < 0.70) {
                    evento(t, TipoEventoSeguranca.ATAQUE_EXECUTADO, u, EventoExperimental.RECURSO_ATAQUE,
                            rnd.nextInt(3), ip, ResultadoEvento.SUCESSO, null, -1);
                } else if (r < 0.90) {
                    escrita(u, t, ip, EventoExperimental.RECURSO_AVISO);
                } else {
                    estego(u, t, ip, r < 0.95);
                }
            }
        }
    }

    /** Ler aviso ou topico = decifrar o conteudo. 0,3% de falha benigna (registro antigo, chave rotacionada). */
    private void leitura(int u, long t, int ip) {
        byte tipoRecurso = rnd.nextDouble() < 0.7 ? EventoExperimental.RECURSO_AVISO : EventoExperimental.RECURSO_TOPICO;
        boolean falha = rnd.nextDouble() < 0.003;
        evento(t, falha ? TipoEventoSeguranca.DECIFRAGEM_FALHA : TipoEventoSeguranca.DECIFRAGEM_OK, u, tipoRecurso,
                recursoDaTurma(u), ip, falha ? ResultadoEvento.FALHA : ResultadoEvento.SUCESSO, null, -1);
    }

    private void escrita(int u, long t, int ip, byte tipoRecurso) {
        boolean falha = rnd.nextDouble() < 0.001;
        evento(t, falha ? TipoEventoSeguranca.CIFRAGEM_FALHA : TipoEventoSeguranca.CIFRAGEM_OK, u, tipoRecurso,
                recursoDaTurma(u), ip, falha ? ResultadoEvento.FALHA : ResultadoEvento.SUCESSO, null, -1);
    }

    private void estego(int u, long t, int ip, boolean embutir) {
        // Extrair de uma imagem sem payload falha; embutir so falha por capacidade, mais raro.
        boolean falha = rnd.nextDouble() < (embutir ? 0.005 : 0.02);
        evento(t, embutir ? TipoEventoSeguranca.ESTEGO_EMBUTIR : TipoEventoSeguranca.ESTEGO_EXTRAIR, u,
                embutir ? EventoExperimental.RECURSO_ESTEGO_EMBUTIR : EventoExperimental.RECURSO_ESTEGO_EXTRAIR, 0, ip,
                falha ? ResultadoEvento.FALHA : ResultadoEvento.SUCESSO, null, -1);
    }

    /** Aviso popular da turma do usuario: poucos ids concentram a maior parte dos acessos. */
    private int recursoDaTurma(int u) {
        int[] minhas = turmasDoUsuario[u];
        int turma = minhas[rnd.nextInt(minhas.length)];
        int j = 1 + Math.min(AVISOS_POR_TURMA - 1, (int) exponencial(8));
        return turma * 1_000 + j;
    }

    // ------------------------------------------------------------------ incidentes

    private void injetarIncidentes() {
        int porCenario = Math.max(1, (int) Math.round(quantidade / (double) EVENTOS_POR_INCIDENTE_DE_CADA_CENARIO));
        int id = 0;
        for (Cenario cenario : Cenario.values()) {
            for (int k = 0; k < porCenario; k++) {
                id++;
                int antes = eventos.size();
                int u = switch (cenario) {
                    case VIOLACAO_INTEGRIDADE -> usuarioComPerfil(PERFIL_PROFESSOR);
                    case ACESSO_ATIPICO -> usuarioComPerfil(PERFIL_ALUNO);
                    default -> rnd.nextInt(perfil.length);
                };
                switch (cenario) {
                    case FORCA_BRUTA_LOGIN -> forcaBruta(u, id);
                    case FALHAS_DECIFRAGEM -> falhasDecifragem(u, id);
                    case VIOLACAO_INTEGRIDADE -> violacaoIntegridade(u, id);
                    case ACESSO_ATIPICO -> acessoAtipico(u, id);
                    case GERACAO_AUTOMATIZADA -> geracaoAutomatizada(u, id);
                }
                List<EventoExperimental> meus = eventos.subList(antes, eventos.size());
                incidentes.add(new Incidente(id, cenario, PRIMEIRO_USUARIO + u, meus.get(0).timestamp(),
                        meus.get(meus.size() - 1).timestamp(), meus.size()));
            }
        }
    }

    /** 15 a 60 senhas erradas, intervalo medio de 3 s, de um IP qualquer; 30% terminam com a senha certa. */
    private void forcaBruta(int u, int id) {
        long t = instanteUniforme();
        int ip = rnd.nextInt(IPS_DISPONIVEIS);
        int tentativas = 15 + rnd.nextInt(46);
        for (int i = 0; i < tentativas; i++) {
            evento(t, TipoEventoSeguranca.LOGIN_FALHA, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.FALHA,
                    Cenario.FORCA_BRUTA_LOGIN, id);
            t += 200 + (long) exponencial(3_000);
        }
        if (rnd.nextDouble() < 0.3) {
            evento(t, TipoEventoSeguranca.LOGIN_SUCESSO, u, EventoExperimental.RECURSO_LOGIN, 0, ip,
                    ResultadoEvento.SUCESSO, Cenario.FORCA_BRUTA_LOGIN, id);
        }
    }

    /** Login normal seguido de 10 a 40 decifragens com falha (ciphertext adulterado), intervalo medio de 8 s. */
    private void falhasDecifragem(int u, int id) {
        long t = instanteDeAula();
        int ip = ip1[u];
        evento(t, TipoEventoSeguranca.LOGIN_SUCESSO, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.SUCESSO,
                Cenario.FALHAS_DECIFRAGEM, id);
        int falhas = 10 + rnd.nextInt(31);
        for (int i = 0; i < falhas; i++) {
            t += 500 + (long) exponencial(8_000);
            byte tipoRecurso = rnd.nextDouble() < 0.7 ? EventoExperimental.RECURSO_AVISO : EventoExperimental.RECURSO_TOPICO;
            evento(t, TipoEventoSeguranca.DECIFRAGEM_FALHA, u, tipoRecurso, recursoDaTurma(u), ip, ResultadoEvento.FALHA,
                    Cenario.FALHAS_DECIFRAGEM, id);
        }
    }

    /** 1 a 4 arquivos do professor com hash diferente do esperado, em sequencia. */
    private void violacaoIntegridade(int u, int id) {
        long t = instanteUniforme();
        int violacoes = 1 + rnd.nextInt(4);
        for (int i = 0; i < violacoes; i++) {
            evento(t, TipoEventoSeguranca.INTEGRIDADE_VIOLADA, u, EventoExperimental.RECURSO_ARQUIVO,
                    1 + rnd.nextInt(5_000), ip1[u], ResultadoEvento.ALERTA, Cenario.VIOLACAO_INTEGRIDADE, id);
            t += 1_000 + (long) exponencial(20_000);
        }
    }

    /** Aluno, entre 1h e 5h locais, lendo 25 a 80 avisos de turmas que nao sao as dele. */
    private void acessoAtipico(int u, int id) {
        int dia = sortear(diaAcumulado);
        int hora = 1 + rnd.nextInt(4);
        long t = instanteLocal(dia, hora);
        int ip = rnd.nextDouble() < 0.5 ? ip1[u] : rnd.nextInt(IPS_DISPONIVEIS);
        evento(t, TipoEventoSeguranca.LOGIN_SUCESSO, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.SUCESSO,
                Cenario.ACESSO_ATIPICO, id);
        int leituras = 25 + rnd.nextInt(56);
        for (int i = 0; i < leituras; i++) {
            t += 500 + (long) exponencial(6_000);
            int recurso = rnd.nextInt(turmas) * 1_000 + 1 + rnd.nextInt(AVISOS_POR_TURMA);
            evento(t, TipoEventoSeguranca.DECIFRAGEM_OK, u, EventoExperimental.RECURSO_AVISO, recurso, ip,
                    ResultadoEvento.SUCESSO, Cenario.ACESSO_ATIPICO, id);
        }
    }

    /**
     * Robo: 60 a 200 leituras de avisos da propria turma (recursos com cara
     * normal) a intervalo quase fixo entre 1 e 3 s, com +-50 ms de variacao.
     * So o ritmo e o volume denunciam.
     */
    private void geracaoAutomatizada(int u, int id) {
        long t = instanteDeAula();
        int ip = ip1[u];
        evento(t, TipoEventoSeguranca.LOGIN_SUCESSO, u, EventoExperimental.RECURSO_LOGIN, 0, ip, ResultadoEvento.SUCESSO,
                Cenario.GERACAO_AUTOMATIZADA, id);
        long passo = 1_000 + rnd.nextInt(2_001);
        int leituras = 60 + rnd.nextInt(141);
        for (int i = 0; i < leituras; i++) {
            t += passo - 50 + rnd.nextInt(101);
            evento(t, TipoEventoSeguranca.DECIFRAGEM_OK, u, EventoExperimental.RECURSO_AVISO, recursoDaTurma(u), ip,
                    ResultadoEvento.SUCESSO, Cenario.GERACAO_AUTOMATIZADA, id);
        }
    }

    private int usuarioComPerfil(int alvo) {
        // Sempre existe pelo menos um de cada (indices 0 e 1); aluno quase sempre existe, mas cai no 1 se nao.
        for (int tentativa = 0; tentativa < 1_000; tentativa++) {
            int u = rnd.nextInt(perfil.length);
            if (perfil[u] == alvo) {
                return u;
            }
        }
        return alvo == PERFIL_ADMIN ? 0 : 1;
    }

    // ------------------------------------------------------------------ tempo e sorteio

    private long instanteDeAula() {
        return instanteLocal(sortear(diaAcumulado), sortear(horaAcumulada));
    }

    /** Qualquer momento do periodo, sem preferencia de hora: ataque nao segue horario de aula. */
    private long instanteUniforme() {
        return instanteLocal(rnd.nextInt(DIAS), rnd.nextInt(24));
    }

    private long instanteLocal(int dia, int hora) {
        LocalDateTime local = INICIO.plusDays(dia).atTime(hora, 0);
        return local.toEpochSecond(FUSO_AULAS) * 1_000 + rnd.nextLong(3_600_000L);
    }

    private void evento(long t, TipoEventoSeguranca tipo, int u, byte tipoRecurso, int idRecurso, int ip,
                        ResultadoEvento resultado, Cenario cenario, int incidente) {
        eventos.add(new EventoExperimental(t, tipo, PRIMEIRO_USUARIO + u, perfil[u], tipoRecurso, idRecurso, ip,
                resultado, cenario, incidente));
    }

    private double exponencial(double media) {
        return -media * Math.log(1.0 - rnd.nextDouble());
    }

    /** Box-Muller; SplittableRandom nao tem nextGaussian no Java 17. */
    private double gaussiana() {
        double u1 = 1.0 - rnd.nextDouble();
        double u2 = rnd.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    private int sortear(double[] acumulado) {
        double alvo = rnd.nextDouble() * acumulado[acumulado.length - 1];
        int i = Arrays.binarySearch(acumulado, alvo);
        int indice = i >= 0 ? i + 1 : -i - 1;
        return Math.min(indice, acumulado.length - 1);
    }

    private static double[] acumular(double[] pesos) {
        double[] acumulado = new double[pesos.length];
        double soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += pesos[i];
            acumulado[i] = soma;
        }
        return acumulado;
    }
}
