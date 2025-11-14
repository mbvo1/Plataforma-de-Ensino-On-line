package dev.com.sigea.dominio.gestaoacademica.estruturaacademica;

import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoStatus;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class ControlarEstruturaAcademicaFeature {
    private PeriodoLetivoRepositoryEmMemoria periodoLetivoRepository;
    private DisciplinaRepositoryEmMemoria disciplinaRepository;
    private EstruturaAcademicaService estruturaAcademicaService;
    private String mensagemDeErro;
    private int totalPeriodosAntes, totalDisciplinasAntes;
    private Date paraData(String dataStr) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dataStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setup() {
        periodoLetivoRepository = new PeriodoLetivoRepositoryEmMemoria();
        disciplinaRepository = new DisciplinaRepositoryEmMemoria();
        estruturaAcademicaService = new EstruturaAcademicaService(periodoLetivoRepository, disciplinaRepository);
        mensagemDeErro = null;
    }

    @Dado("que sou um Administrador logado")
    public void que_sou_um_administrador_logado() {
    }

    @Quando("eu defino as datas de início como {string} e fim como {string} para o novo período letivo {string}")
    public void eu_defino_as_datas_de_início_e_fim(String inicioStr, String fimStr, String id) {
        estruturaAcademicaService.criarPeriodoLetivo(id, paraData(inicioStr), paraData(fimStr));
    }

    @Então("um novo período letivo {string} com status {string} deve existir no sistema")
    public void um_novo_período_letivo_com_status_deve_existir(String id, String status) {
        var salvo = estruturaAcademicaService.buscarPeriodoPorIdentificador(id).orElse(null);
        assertNotNull(salvo);
        assertEquals(PeriodoStatus.valueOf(status), salvo.getStatus());
    }

    @Dado("que já existe um período letivo com datas de {string} a {string}")
    public void que_ja_existe_um_periodo_letivo_com_datas_de_a(String inicio, String fim) {
        estruturaAcademicaService.criarPeriodoLetivo("Periodo Existente", paraData(inicio), paraData(fim));
    }

    @Quando("eu tento criar um novo período letivo com início em {string} e fim em {string}")
    public void eu_tento_criar_um_novo_periodo_letivo_com_inicio_em_e_fim_em(String inicio, String fim) {
        totalPeriodosAntes = periodoLetivoRepository.totalDePeriodos();
        try {
            estruturaAcademicaService.criarPeriodoLetivo("Periodo Conflitante", paraData(inicio), paraData(fim));
        } catch (IllegalStateException e) {
            this.mensagemDeErro = e.getMessage();
        }
    }

    @Então("o sistema deve me impedir com a mensagem {string}")
    public void o_sistema_deve_me_impedir_com_a_mensagem(String msg) {
        assertEquals(msg, this.mensagemDeErro);
    }

    @E("nenhum novo período letivo deve ser criado")
    public void nenhum_novo_periodo_letivo_deve_ser_criado() {
        assertEquals(totalPeriodosAntes, periodoLetivoRepository.totalDePeriodos());
    }

    @Quando("eu crio uma nova disciplina com o nome {string}")
    public void eu_crio_uma_nova_disciplina_com_o_nome(String nome) {
        estruturaAcademicaService.criarDisciplina(nome);
    }

    @Então("a disciplina {string} deve ser criada com sucesso")
    public void a_disciplina_deve_ser_criada_com_sucesso(String nome) {
        assertTrue(estruturaAcademicaService.buscarDisciplinaPorNome(nome).isPresent());
    }

    @Dado("que já existe uma disciplina com o nome {string}")
    public void que_ja_existe_uma_disciplina_com_o_nome(String nome) {
        estruturaAcademicaService.criarDisciplina(nome);
    }

    @Quando("eu tento criar uma nova disciplina com o nome {string}")
    public void eu_tento_criar_uma_nova_disciplina(String nome) {
        totalDisciplinasAntes = disciplinaRepository.totalDeDisciplinas();
        try {
            estruturaAcademicaService.criarDisciplina(nome);
        } catch (IllegalStateException e) {
            this.mensagemDeErro = e.getMessage();
        }
    }
}