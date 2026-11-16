package dev.com.sigea.apresentacao.auditoria;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.com.sigea.apresentacao.seguranca.JwtUtil;
import dev.com.sigea.infraestrutura.persistencia.AvisoEntity;
import dev.com.sigea.infraestrutura.persistencia.AvisoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.CifraConteudo;
import dev.com.sigea.infraestrutura.persistencia.RecifragemConteudoLegado;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CifraSigea ligada ao app: o conteudo de avisos vai cifrado para o banco,
 * cada operacao entra no log encadeado, e a tela de esteganografia esconde
 * e recupera o texto cifrado de um aviso de verdade.
 *
 * Banco em memoria proprio (nao o "fase4"): o seeder da Fase 4 apaga o log
 * inteiro, e as contagens de evento daqui nao podem depender da ordem em que
 * o JUnit roda as classes.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cifra-conteudo;DB_CLOSE_DELAY=-1",
        "jwt.secret=segredo-so-de-teste-com-mais-de-32-bytes-0123456789",
        "gemini.api.key=teste",
        "cpf.encryption.key=chave-so-de-teste",
        "spring.jpa.show-sql=false",
        "file.upload-dir=target/uploads-teste"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CifraConteudoEsteganografiaIntegracaoTest {

    private static final String TEXTO = "Prova de Calculo I remarcada para sexta, 14h, sala 302. "
            + "Tragam documento com foto; a revisao comeca 30 minutos antes.";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private JwtUtil jwt;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private AvisoJpaRepository avisos;
    @Autowired
    private RecifragemConteudoLegado recifragem;

    private String tokenAdmin;
    private long idAdmin;

    @BeforeAll
    void autenticar() {
        idAdmin = jdbc.queryForObject("SELECT usuario_id FROM Usuarios WHERE email = 'admin@sigea.com'", Long.class);
        tokenAdmin = jwt.gerarToken(String.valueOf(idAdmin), "admin@sigea.com", "ADMINISTRADOR");
    }

    @Test
    @DisplayName("Aviso salvo pela entidade vai para o banco como v1:Base64, volta em claro e gera CIFRAGEM_OK e DECIFRAGEM_OK")
    void avisoCifradoEmRepouso() {
        long antesCifragem = eventos("CIFRAGEM_OK");
        long antesDecifragem = eventos("DECIFRAGEM_OK");

        long id = avisos.save(new AvisoEntity("Prova remarcada", TEXTO, idAdmin, "GERAL")).getId();

        String coluna = colunaDoAviso(id);
        assertTrue(coluna.startsWith("v1:"), coluna);
        assertFalse(coluna.contains("Calculo"));
        assertEquals(TEXTO, avisos.findById(id).orElseThrow().getConteudo());
        assertEquals(antesCifragem + 1, eventos("CIFRAGEM_OK"));
        assertTrue(eventos("DECIFRAGEM_OK") > antesDecifragem);

        // Nonce novo a cada gravacao: o mesmo texto vira outro texto cifrado.
        long outro = avisos.save(new AvisoEntity("Prova remarcada", TEXTO, idAdmin, "GERAL")).getId();
        assertNotEquals(coluna, colunaDoAviso(outro));
    }

    @Test
    @DisplayName("Fraqueza do CBC sem autenticacao: virar um bit no 1o bloco cifrado decifra para texto alterado, sem erro")
    void adulteracaoNoMeioPassaCalada() {
        long id = avisos.save(new AvisoEntity("Adulteracao", TEXTO, idAdmin, "GERAL")).getId();
        byte[] payload = Base64.getDecoder().decode(colunaDoAviso(id).substring(3));
        // nonce (12 bytes) e depois o 1o bloco: o bit virado embaralha o 1o
        // bloco de texto claro e vira o mesmo bit no 2o. O padding, no ultimo
        // bloco, nao e tocado - entao nada acusa.
        payload[12] ^= 0x01;
        gravarColuna(id, "v1:" + Base64.getEncoder().encodeToString(payload));
        long falhasAntes = eventos("DECIFRAGEM_FALHA");

        String lido = avisos.findById(id).orElseThrow().getConteudo();

        assertNotEquals(TEXTO, lido);
        assertNotEquals(CifraConteudo.CONTEUDO_INDISPONIVEL, lido);
        assertTrue(lido.endsWith(TEXTO.substring(32)), lido);
        assertEquals(falhasAntes, eventos("DECIFRAGEM_FALHA"));
    }

    @Test
    @DisplayName("Texto cifrado truncado vira DECIFRAGEM_FALHA e o aviso mostra 'indisponivel' em vez de derrubar a leitura")
    void truncadoViraFalhaRegistrada() {
        long id = avisos.save(new AvisoEntity("Truncado", TEXTO, idAdmin, "GERAL")).getId();
        String coluna = colunaDoAviso(id);
        gravarColuna(id, coluna.substring(0, 20));
        long falhasAntes = eventos("DECIFRAGEM_FALHA");

        assertEquals(CifraConteudo.CONTEUDO_INDISPONIVEL, avisos.findById(id).orElseThrow().getConteudo());
        assertEquals(falhasAntes + 1, eventos("DECIFRAGEM_FALHA"));
        assertEquals("OK", verificarCadeia().get("status").asText());
    }

    @Test
    @DisplayName("Linha legada em texto claro e cifrada pelo runner da subida, e so ela")
    void legadoEmClaroECifradoNaSubida() throws Exception {
        long id = avisos.save(new AvisoEntity("Legado", TEXTO, idAdmin, "GERAL")).getId();
        gravarColuna(id, "aviso antigo, gravado antes da cifra");
        String jaCifrado = colunaDoAviso(avisos.save(new AvisoEntity("Novo", TEXTO, idAdmin, "GERAL")).getId());

        recifragem.run(new DefaultApplicationArguments());

        assertTrue(colunaDoAviso(id).startsWith("v1:"));
        assertEquals("aviso antigo, gravado antes da cifra", avisos.findById(id).orElseThrow().getConteudo());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM avisos WHERE conteudo = ?", Integer.class, jaCifrado));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM avisos WHERE conteudo NOT LIKE 'v1:%'", Integer.class));
    }

    @Test
    @DisplayName("Aceite da Fase 3 pela tela: esconde o texto cifrado de um aviso num PNG, extrai, decifra e obtem o original")
    void idaEVoltaPeloEndpoint() throws Exception {
        long id = avisos.save(new AvisoEntity("Estego", TEXTO, idAdmin, "GERAL")).getId();
        long embutirAntes = eventos("ESTEGO_EMBUTIR");
        long extrairAntes = eventos("ESTEGO_EXTRAIR");

        MvcResult r = mvc.perform(multipart("/api/admin/esteganografia/embutir")
                        .file(new MockMultipartFile("imagem", "capa.png", "image/png", png(200, 150)))
                        .param("avisoId", String.valueOf(id))
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk()).andReturn();
        JsonNode embutido = json.readTree(r.getResponse().getContentAsString());
        byte[] estego = Base64.getDecoder().decode(embutido.get("imagemPng").asText());

        // O que foi escondido e exatamente a coluna do banco, ja cifrada.
        assertEquals(colunaDoAviso(id).length(), embutido.get("payloadBytes").asInt());
        assertTrue(embutido.get("psnrDb").asDouble() > 40, embutido.toString());

        MvcResult x = mvc.perform(multipart("/api/admin/esteganografia/extrair")
                        .file(new MockMultipartFile("imagem", "estego.png", "image/png", estego))
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk()).andReturn();
        assertEquals(TEXTO, json.readTree(x.getResponse().getContentAsString()).get("textoClaro").asText());
        assertEquals(embutirAntes + 1, eventos("ESTEGO_EMBUTIR"));
        assertEquals(extrairAntes + 1, eventos("ESTEGO_EXTRAIR"));
    }

    @Test
    @DisplayName("Allowlist propria: JPG e recusado com erro explicito; imagem sem payload nao decifra")
    void jpgRecusadoEImagemLimpaNaoDecifra() throws Exception {
        MvcResult jpg = mvc.perform(multipart("/api/admin/esteganografia/embutir")
                        .file(new MockMultipartFile("imagem", "foto.png", "image/png", imagem(200, 150, "jpg")))
                        .param("texto", "segredo")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest()).andReturn();
        assertTrue(jpg.getResponse().getContentAsString().contains("JPEG"), jpg.getResponse().getContentAsString());

        mvc.perform(multipart("/api/admin/esteganografia/extrair")
                        .file(new MockMultipartFile("imagem", "limpa.png", "image/png", png(200, 150)))
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(r -> assertTrue(r.getResponse().getStatus() == 400 || r.getResponse().getStatus() == 422,
                        "status " + r.getResponse().getStatus()));
    }

    @Test
    @DisplayName("Esteganografia so para ADMINISTRADOR")
    void soAdministrador() throws Exception {
        String aluno = jwt.gerarToken("999", "aluno@sigea.com", "ALUNO");
        mvc.perform(get("/api/admin/esteganografia/avisos").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/admin/esteganografia/extrair")
                        .file(new MockMultipartFile("imagem", "x.png", "image/png", png(20, 20)))
                        .header("Authorization", "Bearer " + aluno))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ apoio

    private String colunaDoAviso(long id) {
        return jdbc.queryForObject("SELECT conteudo FROM avisos WHERE aviso_id = ?", String.class, id);
    }

    private void gravarColuna(long id, String valor) {
        jdbc.update("UPDATE avisos SET conteudo = ? WHERE aviso_id = ?", valor, id);
    }

    private long eventos(String tipo) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM log_seguranca WHERE tipo_evento = ?", Long.class, tipo);
    }

    private JsonNode verificarCadeia() {
        try {
            MvcResult r = mvc.perform(get("/api/admin/auditoria/verificar-integridade")
                    .header("Authorization", "Bearer " + tokenAdmin)).andExpect(status().isOk()).andReturn();
            return json.readTree(r.getResponse().getContentAsString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] png(int largura, int altura) throws IOException {
        return imagem(largura, altura, "png");
    }

    /** Ruido com semente fixa: o mesmo arquivo em toda execucao. */
    private static byte[] imagem(int largura, int altura, String formato) throws IOException {
        BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Random r = new Random(42);
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                img.setRGB(x, y, r.nextInt(0x1000000));
            }
        }
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(img, formato, saida);
        return saida.toByteArray();
    }
}
