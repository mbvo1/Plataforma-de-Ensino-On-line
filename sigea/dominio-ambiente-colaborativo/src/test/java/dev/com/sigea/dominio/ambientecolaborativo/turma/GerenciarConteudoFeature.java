package dev.com.sigea.dominio.ambientecolaborativo.turma;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciarConteudoFeature {

    private TurmaRepository turmaRepository;
    private TurmaService turmaService;
    private Turma turma;
    private Exception excecaoCapturada;

    @Before
    public void setup() {
        turmaRepository = new TurmaRepositoryEmMemoria();
        turmaService = new TurmaService(turmaRepository);
        excecaoCapturada = null;
    }

    @Dado("que o professor {string} possui uma turma chamada {string}")
    public void que_o_professor_possui_uma_turma_chamada(String idProfessor, String tituloTurma) {
        TurmaId novoId = turmaRepository.proximoId();
        UsuarioId professorId = new UsuarioId(idProfessor);
        turma = new Turma(novoId, professorId, tituloTurma);
        turmaRepository.salvar(turma);
    }

    @Quando("o professor {string} publica na turma um novo material com título {string}, descrição {string} e anexa o arquivo {string}")
    public void o_professor_publica_na_turma_um_novo_material(String idProfessor, String titulo, String descricao, String nomeAnexo) {
        Anexo anexo = new Anexo(nomeAnexo);
        turmaService.publicarMaterialNaTurma(new UsuarioId(idProfessor), turma.getId(), titulo, descricao, List.of(anexo));
    }

    @Então("a turma {string} deve conter um material com o título {string}")
    public void a_turma_deve_conter_um_material_com_o_titulo(String tituloTurma, String tituloMaterial) {
        Turma turmaSalva = turmaRepository.buscarPorId(turma.getId()).orElseThrow();
        boolean materialEncontrado = turmaSalva.getMateriais().stream()
                .anyMatch(m -> m.getTitulo().equals(tituloMaterial));
        assertTrue(materialEncontrado, "O material não foi encontrado na turma.");
    }

    @E("esse material deve ter um anexo chamado {string}")
    public void esse_material_deve_ter_um_anexo_chamado(String nomeAnexo) {
        Material material = turma.getMateriais().get(0);
        boolean anexoEncontrado = material.getAnexos().stream()
                .anyMatch(a -> a.nomeArquivo().equals(nomeAnexo));
        assertTrue(anexoEncontrado, "O anexo não foi encontrado no material.");
    }

    @E("existe outro professor com ID {string}")
    public void existe_outro_professor_com_id(String idProfessor) {

    }

    @Quando("o professor {string} tenta publicar um material na turma do professor {string}")
    public void o_professor_tenta_publicar_um_material_na_turma_do_professor(String idProfessorInvasor, String idProfessorDono) {
        try {
            turmaService.publicarMaterialNaTurma(new UsuarioId(idProfessorInvasor), turma.getId(), "Material Invasor", "Desc", List.of());
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("o sistema deve lançar uma exceção de segurança com a mensagem {string}")
    public void o_sistema_deve_lancar_uma_excecao_de_seguranca_com_a_mensagem(String mensagemEsperada) {
        assertNotNull(excecaoCapturada, "Uma exceção de segurança era esperada.");
        assertInstanceOf(SecurityException.class, excecaoCapturada);
        assertEquals(mensagemEsperada, excecaoCapturada.getMessage());
    }
}