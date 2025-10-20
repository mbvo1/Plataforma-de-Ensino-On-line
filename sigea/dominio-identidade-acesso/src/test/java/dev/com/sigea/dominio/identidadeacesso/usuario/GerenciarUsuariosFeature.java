package dev.com.sigea.dominio.identidadeacesso.usuario;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciarUsuariosFeature {

    private UsuarioRepositoryEmMemoria usuarioRepository;
    private String nome;
    private String email;
    private Perfil perfil;
    private String mensagemDeErro;
    private int totalUsuariosAntesDaOperacao;

    private void setup() {
        usuarioRepository = new UsuarioRepositoryEmMemoria();
        mensagemDeErro = null;
        totalUsuariosAntesDaOperacao = 0;
    }

    @Dado("que eu sou um administrador logado no sistema")
    public void que_eu_sou_um_administrador_logado_no_sistema() {
        setup();
        // Em um cenário real, aqui haveria uma simulação de login
        // Para este teste de domínio, apenas inicializamos o ambiente
    }

    @Quando("eu preencho o formulário de cadastro com o nome {string}, o e-mail {string} e o perfil {string}")
    public void eu_preencho_o_formulario_de_cadastro_com_o_nome_o_email_e_o_perfil(String nome, String email, String perfilStr) {
        this.nome = nome;
        this.email = email;
        this.perfil = Perfil.valueOf(perfilStr);
    }

    @Quando("eu clico em {string}")
    public void eu_clico_em(String botao) {
        Usuario novoUsuario = new Usuario(usuarioRepository.proximoId(), this.nome, this.email, this.perfil);
        usuarioRepository.salvar(novoUsuario);
    }

    @Entao("um novo usuário com o perfil {string} e status {string} deve ser criado")
    public void um_novo_usuario_com_o_perfil_e_status_deve_ser_criado(String perfilEsperado, String statusEsperado) {
        Usuario usuarioSalvo = usuarioRepository.buscarPorEmail(this.email).orElse(null);
        assertNotNull(usuarioSalvo, "O usuário deveria ter sido salvo, mas não foi encontrado.");
        assertEquals(Perfil.valueOf(perfilEsperado), usuarioSalvo.getPerfil());
        assertEquals(UsuarioStatus.valueOf(statusEsperado), usuarioSalvo.getStatus());
    }

    @Entao("o usuário {string} deve existir na lista de professores")
    public void o_usuario_deve_existir_na_lista_de_professores(String email) {
        assertTrue(usuarioRepository.buscarPorEmail(email).isPresent(), "O usuário com o e-mail " + email + " não foi encontrado.");
    }

    // Cenário de Falha
    @Dado("que já existe um usuário com o e-mail {string}")
    public void que_ja_existe_um_usuario_com_o_email(String emailExistente) {
        Usuario usuarioPreexistente = new Usuario(
            usuarioRepository.proximoId(),
            "Professor Existente",
            emailExistente,
            Perfil.PROFESSOR
        );
        usuarioRepository.salvar(usuarioPreexistente);
    }

    @Quando("eu tento criar um novo professor com o nome {string} e o e-mail {string}")
    public void eu_tento_criar_um_novo_professor_com_o_nome_e_o_email(String nome, String email) {
        this.totalUsuariosAntesDaOperacao = usuarioRepository.totalDeUsuarios();
        try {
            Usuario novoUsuario = new Usuario(usuarioRepository.proximoId(), nome, email, Perfil.PROFESSOR);
            usuarioRepository.salvar(novoUsuario);
        } catch (IllegalStateException e) {
            this.mensagemDeErro = e.getMessage();
        }
    }

    @Entao("o sistema deve me informar que {string}")
    public void o_sistema_deve_me_informar_que(String msgEsperada) {
        assertEquals(msgEsperada, this.mensagemDeErro);
    }

    @Entao("nenhum novo usuário deve ser criado com este e-mail")
    public void nenhum_novo_usuario_deve_ser_criado_com_este_e_mail() {
        assertEquals(totalUsuariosAntesDaOperacao, usuarioRepository.totalDeUsuarios(), "O número de usuários não deveria ter aumentado.");
    }
}