package dev.com.sigea.dominio.gestaoacademica.sala;

import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaId;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class GestaoDesempenhoFeature {

    private Sala sala;
    private SalaRepository salaRepository;
    private LancamentoDeNotaService lancamentoDeNotaService;
    private Exception excecaoCapturada;

    @Before
    public void setup() {
        this.salaRepository = new SalaRepositoryEmMemoria();
        this.lancamentoDeNotaService = new LancamentoDeNotaService(salaRepository);
        this.excecaoCapturada = null;
    }

    @Dado("uma sala com o aluno {string} matriculado")
    public void uma_sala_com_o_aluno_matriculado(String idAluno) {
        var disciplinaId = new DisciplinaId("DISC-TESTE-123");
        sala = new Sala(salaRepository.proximoId(), disciplinaId, 10);
        sala.matricular(new UsuarioId(idAluno));
        salaRepository.salvar(sala);
    }

    @Quando("o professor lança a nota {double} na avaliação {string} para o aluno {string}")
    public void o_professor_lanca_a_nota_na_avaliacao_para_o_aluno(Double valorNota, String avaliacao, String idAluno) {
        try {
            lancamentoDeNotaService.lancarNota(new UsuarioId(idAluno), sala.getId(), avaliacao, new Nota(valorNota));
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("a nota {double} na {string} deve ser registrada para o aluno {string}")
    public void a_nota_na_deve_ser_registrada_para_o_aluno(Double valorNota, String avaliacao, String idAluno) {
        assertNull(excecaoCapturada, "Nenhuma exceção era esperada, mas uma foi lançada.");

        Matricula matricula = sala.getMatriculas().stream()
                .filter(m -> m.getAlunoId().valor().equals(idAluno))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Matrícula não encontrada para o aluno."));

        assertEquals(valorNota, matricula.getAvaliacoes().get(avaliacao).valor());
    }

    @Quando("o professor tenta lançar a nota {double} na avaliação {string} para o aluno {string}")
    public void o_professor_tenta_lancar_a_nota_na_avaliacao_para_o_aluno(Double valorNota, String avaliacao, String idAluno) {
        try {
            lancamentoDeNotaService.lancarNota(new UsuarioId(idAluno), sala.getId(), avaliacao, new Nota(valorNota));
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("o sistema deve lançar uma exceção de validação com a mensagem {string}")
    public void o_sistema_deve_lancar_uma_excecao_de_validacao_com_a_mensagem(String mensagemEsperada) {
        assertNotNull(excecaoCapturada, "Uma exceção era esperada, mas não foi lançada.");
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertEquals(mensagemEsperada, excecaoCapturada.getMessage());
    }
}