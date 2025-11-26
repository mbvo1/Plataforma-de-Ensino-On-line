package dev.com.sigea.dominio.turma;

import dev.com.sigea.dominio.usuario.UsuarioId;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class AlunoInterageTurmaFeature {

    private TurmaRepository turmaRepository;
    private IngressoService ingressoService;
    private Turma turma;
    private Exception excecaoCapturada;
    private int numeroParticipantes;

    @Before
    public void setup() {
        turmaRepository = new TurmaRepositoryEmMemoria();
        ingressoService = new IngressoService(turmaRepository);
        excecaoCapturada = null;
    }

    @Dado("que existe uma turma com o código de acesso {string}")
    public void que_existe_uma_turma_com_o_codigo_de_acesso(String codigoAcesso) {
        TurmaId novoId = turmaRepository.proximoId();
        UsuarioId professorId = new UsuarioId("professor-1");
        turma = new Turma(novoId, professorId, "Turma de Teste");
        turmaRepository.salvar(turma);
        
    }

    @Dado("que não existe nenhuma turma com o código {string}")
    public void que_nao_existe_nenhuma_turma_com_o_codigo(String codigoAcesso) {

    }

    @Quando("um aluno com ID {string} tenta ingressar na turma com o código {string}")
    public void um_aluno_com_id_tenta_ingressar_na_turma_com_o_codigo(String idAluno, String codigoAcesso) {
        try {
            UsuarioId alunoId = new UsuarioId(idAluno);
            CodigoAcesso codigo = new CodigoAcesso(codigoAcesso);
            ingressoService.ingressarAluno(codigo, alunoId);
            
            turma = turmaRepository.buscarPorCodigoDeAcesso(codigo).orElse(null);
            if (turma != null) {
                numeroParticipantes = turma.getAlunosParticipantes().size();
            }
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Quando("um aluno com ID {string} tenta ingressar na turma com o código da turma criada")
    public void um_aluno_com_id_tenta_ingressar_na_turma_com_o_codigo_da_turma_criada(String idAluno) {
        try {
            UsuarioId alunoId = new UsuarioId(idAluno);
            CodigoAcesso codigo = turma.getCodigoAcesso();
            ingressoService.ingressarAluno(codigo, alunoId);
            
            turma = turmaRepository.buscarPorCodigoDeAcesso(codigo).orElse(null);
            if (turma != null) {
                numeroParticipantes = turma.getAlunosParticipantes().size();
            }
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("a turma deve ter {int} participante")
    public void a_turma_deve_ter_participante(Integer numeroEsperado) {
        assertEquals(numeroEsperado.intValue(), numeroParticipantes, 
            "Número de participantes não corresponde ao esperado.");
    }

    @Então("a interação com a turma deve retornar erro {string}")
    public void o_sistema_deve_lancar_uma_excecao_com_a_mensagem(String mensagemEsperada) {
        assertNotNull(excecaoCapturada, "Uma exceção era esperada.");
        assertEquals(mensagemEsperada, excecaoCapturada.getMessage());
    }
}