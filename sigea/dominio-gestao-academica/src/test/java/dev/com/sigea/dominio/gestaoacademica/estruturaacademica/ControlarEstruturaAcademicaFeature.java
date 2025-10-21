package dev.com.sigea.dominio.gestaoacademica.estruturaacademica;
import dev.com.sigea.dominio.gestaoacademica.disciplina.Disciplina;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.*;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
public class ControlarEstruturaAcademicaFeature {
    private PeriodoLetivoRepositoryEmMemoria periodoLetivoRepository;
    private DisciplinaRepositoryEmMemoria disciplinaRepository;
    private String mensagemDeErro;
    private int totalPeriodosAntes, totalDisciplinasAntes;
    private Date paraData(String dataStr) {
        try { return new SimpleDateFormat("dd/MM/yyyy").parse(dataStr); }
        catch (ParseException e) { throw new RuntimeException(e); }
    }
    @Before
    public void setup() {
        periodoLetivoRepository = new PeriodoLetivoRepositoryEmMemoria();
        disciplinaRepository = new DisciplinaRepositoryEmMemoria();
        mensagemDeErro = null;
    }
    @Dado("que sou um Administrador logado") public void que_sou_um_administrador_logado() {}
    @Quando("eu defino as datas de início como {string} e fim como {string} para o novo período letivo {string}")
    public void eu_defino_as_datas_de_início_e_fim(String inicioStr, String fimStr, String id) {
        var novoPeriodo = new PeriodoLetivo(periodoLetivoRepository.proximoId(), id, paraData(inicioStr), paraData(fimStr));
        periodoLetivoRepository.salvar(novoPeriodo);
    }
    @Entao("um novo período letivo {string} com status {string} deve existir no sistema")
    public void um_novo_período_letivo_com_status_deve_existir(String id, String status) {
        var salvo = periodoLetivoRepository.buscarPorIdentificador(id).orElse(null);
        assertNotNull(salvo);
        assertEquals(PeriodoStatus.valueOf(status), salvo.getStatus());
    }
    @Dado("que já existe um período letivo {string} com início em {string} e fim em {string}")
    public void que_ja_existe_um_periodo_letivo(String id, String inicio, String fim) {
        periodoLetivoRepository.salvar(new PeriodoLetivo(periodoLetivoRepository.proximoId(), id, paraData(inicio), paraData(fim)));
    }
    @Quando("eu tento criar um novo período letivo {string} com início em {string} e fim em {string}")
    public void eu_tento_criar_um_novo_periodo_letivo(String id, String inicio, String fim) {
        totalPeriodosAntes = periodoLetivoRepository.totalDePeriodos();
        try {
            periodoLetivoRepository.salvar(new PeriodoLetivo(periodoLetivoRepository.proximoId(), id, paraData(inicio), paraData(fim)));
        } catch (IllegalStateException e) {
            this.mensagemDeErro = e.getMessage();
        }
    }
    @Entao("o sistema deve me impedir com a mensagem {string}")
    public void o_sistema_deve_me_impedir_com_a_mensagem(String msg) { assertEquals(msg, this.mensagemDeErro); }
    @Entao("o período letivo {string} não deve ser criado")
    public void o_periodo_letivo_nao_deve_ser_criado(String id) {
        assertEquals(totalPeriodosAntes, periodoLetivoRepository.totalDePeriodos());
    }
    @Quando("eu crio uma nova disciplina com o nome {string}")
    public void eu_crio_uma_nova_disciplina_com_o_nome(String nome) {
        disciplinaRepository.salvar(new Disciplina(disciplinaRepository.proximoId(), nome));
    }
    @Entao("a disciplina {string} deve ser criada com sucesso")
    public void a_disciplina_deve_ser_criada_com_sucesso(String nome) {
        assertTrue(disciplinaRepository.buscarPorNome(nome).isPresent());
    }
    @Dado("que já existe uma disciplina com o nome {string}")
    public void que_ja_existe_uma_disciplina_com_o_nome(String nome) {
        disciplinaRepository.salvar(new Disciplina(disciplinaRepository.proximoId(), nome));
    }
    @Quando("eu tento criar uma nova disciplina com o nome {string}")
    public void eu_tento_criar_uma_nova_disciplina(String nome) {
        totalDisciplinasAntes = disciplinaRepository.totalDeDisciplinas();
        try {
            disciplinaRepository.salvar(new Disciplina(disciplinaRepository.proximoId(), nome));
        } catch (IllegalStateException e) {
            this.mensagemDeErro = e.getMessage();
        }
    }
}