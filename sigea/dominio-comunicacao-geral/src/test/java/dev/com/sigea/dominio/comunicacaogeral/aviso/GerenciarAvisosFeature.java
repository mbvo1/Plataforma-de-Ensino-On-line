package dev.com.sigea.dominio.comunicacaogeral.aviso;

import dev.com.sigea.dominio.ambientecolaborativo.turma.*;
import dev.com.sigea.dominio.comunicacaogeral.aviso.*;
import dev.com.sigea.dominio.identidadeacesso.usuario.*;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;
import static org.junit.jupiter.api.Assertions.*;

public class GerenciarAvisosFeature {
    private UsuarioRepository usuarioRepository;
    private TurmaRepository turmaRepository;
    private AvisoRepository avisoRepository;
    private PublicadorDeAvisoService publicadorService;
    private UsuarioId profCarlosId, profAnaId;
    private TurmaId turmaCalculoId;
    private Exception excecaoCapturada;
    private Aviso avisoCriado;

    @Before
    public void setup() {
        usuarioRepository = new UsuarioRepositoryEmMemoria();
        turmaRepository = new TurmaRepositoryEmMemoria();
        avisoRepository = new AvisoRepositoryEmMemoria();
        publicadorService = new PublicadorDeAvisoService(avisoRepository, turmaRepository);
        excecaoCapturada = null;
    }

    @Dado("que existe um professor com email {string} e senha {string}")
    public void que_existe_um_professor(String email, String senha) {
        Usuario prof = new Usuario(usuarioRepository.proximoId(), "Prof " + email, email, Perfil.PROFESSOR);
        usuarioRepository.salvar(prof);
        if (email.equals("carlos.souza@sigea.com.br")) profCarlosId = prof.getId();
    }

    @E("que o professor {string} é responsável pela turma {string}")
    public void que_o_professor_e_responsavel(String email, String nomeTurma) {
        Turma turma = new Turma(turmaRepository.proximoId(), profCarlosId, nomeTurma);
        turmaRepository.salvar(turma);
        turmaCalculoId = turma.getId();
    }

    @E("que existe um outro professor com email {string}")
    public void que_existe_outro_professor(String email) {
        Usuario prof = new Usuario(usuarioRepository.proximoId(), "Prof " + email, email, Perfil.PROFESSOR);
        usuarioRepository.salvar(prof);
        profAnaId = prof.getId();
    }

    @Quando("o professor {string} publica um aviso na turma {string} com o título {string} e conteúdo {string}")
    public void o_professor_publica_aviso(String email, String turma, String titulo, String conteudo) {
        avisoCriado = publicadorService.publicarParaTurma(profCarlosId, turmaCalculoId, titulo, conteudo);
    }

    @Então("um aviso com título {string} deve ser criado")
    public void um_aviso_deve_ser_criado(String titulo) {
        assertNotNull(avisoCriado);
        assertEquals(titulo, avisoCriado.getTitulo());
    }

    @E("o aviso deve estar associado à turma {string}")
    public void o_aviso_deve_estar_associado(String nomeTurma) {
        assertEquals(turmaCalculoId, avisoCriado.getTurmaId());
    }

    @Quando("o professor {string} tenta publicar um aviso na turma {string} com o título {string}")
    public void o_professor_tenta_publicar_aviso(String email, String turma, String titulo) {
        try {
            publicadorService.publicarParaTurma(profAnaId, turmaCalculoId, titulo, "");
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o sistema deve lançar uma exceção de {string} com a mensagem {string}")
    public void o_sistema_deve_lancar_excecao(String tipo, String mensagem) {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(SecurityException.class, excecaoCapturada);
        assertEquals(mensagem, excecaoCapturada.getMessage());
    }
}