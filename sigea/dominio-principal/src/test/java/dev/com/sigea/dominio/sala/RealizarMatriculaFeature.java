package dev.com.sigea.dominio.sala;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.usuario.UsuarioId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class RealizarMatriculaFeature {

    private SalaRepository salaRepository;
    private MatriculaService matriculaService;
    private Sala sala;
    private UsuarioId alunoId;
    private Exception excecaoCapturada;

    @Dado("uma sala da disciplina {string} com {int} vaga(s)")
    public void uma_sala_da_disciplina_com_vagas(String nomeDisciplina, int vagas) {
        this.salaRepository = new SalaRepositoryEmMemoria();
        this.matriculaService = new MatriculaService(salaRepository);
        var disciplinaId = new DisciplinaId("DISC-" + nomeDisciplina.hashCode());
        this.sala = new Sala(salaRepository.proximoId(), disciplinaId, vagas);
        this.salaRepository.salvar(sala);
    }

    @E("um aluno com ID {string}")
    public void um_aluno_com_id(String id) {
        this.alunoId = new UsuarioId(id);
    }

    @Quando("o aluno {string} se matricula na sala")
    public void o_aluno_se_matricula_na_sala(String id) {
        try {
            matriculaService.realizarMatricula(new UsuarioId(id), sala.getId());
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("a matrícula deve ser confirmada")
    public void a_matricula_deve_ser_confirmada() {
        assertNull(excecaoCapturada, "Nenhuma exceção era esperada.");
        boolean alunoEstaMatriculado = sala.getMatriculas()
                .stream()
                .anyMatch(m -> m.getAlunoId().equals(alunoId));
        assertTrue(alunoEstaMatriculado, "O aluno deveria estar na lista de matriculados.");
    }

    @E("a sala deve ter {int} vagas restantes")
    public void a_sala_deve_ter_vagas_restantes(int vagasEsperadas) {
        assertEquals(vagasEsperadas, sala.getVagasRestantes());
    }

    @Quando("o aluno {string} tenta se matricular na sala")
    public void o_aluno_tenta_se_matricular_na_sala(String id) {
        // A implementação é a mesma do passo "Quando" de sucesso
        o_aluno_se_matricula_na_sala(id);
    }

    @Então("o processo de matrícula deve falhar com a mensagem {string}")
    public void o_sistema_deve_lancar_uma_excecao_com_a_mensagem(String mensagemEsperada) {
        assertNotNull(excecaoCapturada, "Uma exceção era esperada, mas não foi lançada.");
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertEquals(mensagemEsperada, excecaoCapturada.getMessage());
    }
}