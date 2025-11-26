package dev.com.sigea.dominio.forum;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.sala.SalaRepositoryEmMemoria;
import dev.com.sigea.dominio.usuario.UsuarioId;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciarForumFeature {

    private SalaRepository salaRepository;
    private TopicoRepository topicoRepository;
    private ForumService forumService;
    private Exception excecaoCapturada;
    private Topico topicoCriado;

    @Before
    public void setup() {
        salaRepository = new SalaRepositoryEmMemoria();
        topicoRepository = new TopicoRepositoryEmMemoria();
        forumService = new ForumService(salaRepository, topicoRepository);
        excecaoCapturada = null;
        topicoCriado = null;
    }

    @Dado("que a aluna {string} está matriculada na disciplina {string}")
    public void que_a_aluna_esta_matriculada_na_disciplina(String idAluna, String idDisciplina) {
        Sala sala = new Sala(salaRepository.proximoId(), new DisciplinaId(idDisciplina), 10);
        sala.matricular(new UsuarioId(idAluna));
        salaRepository.salvar(sala);
    }

    @Quando("a aluna {string} cria um tópico com o título {string} no fórum da disciplina {string}")
    public void a_aluna_cria_um_topico_com_o_titulo_no_forum_da_disciplina(String idAluna, String titulo, String idDisciplina) {
        try {
            topicoCriado = forumService.criarTopico(
                new UsuarioId(idAluna),
                new DisciplinaId(idDisciplina),
                titulo,
                "Este é o conteúdo da minha dúvida..."
            );
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("o tópico {string} deve ser criado com sucesso")
    public void o_topico_deve_ser_criado_com_sucesso(String titulo) {
        assertNull(excecaoCapturada, "Nenhuma exceção era esperada.");
        assertNotNull(topicoCriado);
        assertEquals(titulo, topicoCriado.getTitulo());
    }

    @Dado("que o aluno {string} não está matriculado na disciplina {string}")
    public void que_o_aluno_nao_esta_matriculado_na_disciplina(String idAluno, String idDisciplina) {
    }

    @Quando("o aluno {string} tenta criar um tópico no fórum da disciplina {string}")
    public void o_aluno_tenta_criar_um_topico_no_forum_da_disciplina(String idAluno, String idDisciplina) {
        try {
            forumService.criarTopico(
                new UsuarioId(idAluno),
                new DisciplinaId(idDisciplina),
                "Título de Teste",
                "Conteúdo de Teste"
            );
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("o sistema deve retornar um erro de acesso não autorizado com a mensagem {string}")
    public void o_sistema_deve_retornar_um_erro_de_acesso_nao_autorizado_com_a_mensagem(String mensagem) {
        assertNotNull(excecaoCapturada, "Uma exceção era esperada.");
        assertInstanceOf(SecurityException.class, excecaoCapturada);
        assertEquals(mensagem, excecaoCapturada.getMessage());
    }
}