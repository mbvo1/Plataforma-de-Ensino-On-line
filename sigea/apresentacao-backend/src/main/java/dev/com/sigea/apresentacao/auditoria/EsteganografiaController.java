package dev.com.sigea.apresentacao.auditoria;

import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.dominio.cripto.CifraSigea;
import dev.com.sigea.dominio.cripto.estego.AnaliseEsteganografia;
import dev.com.sigea.dominio.cripto.estego.AnaliseEsteganografia.ResultadoQuiQuadrado;
import dev.com.sigea.dominio.cripto.estego.EsteganografiaLSB;
import dev.com.sigea.infraestrutura.auditoria.RegistradorLogSeguranca;
import dev.com.sigea.infraestrutura.persistencia.CifraConteudo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Projeto 2, Fase 3 na tela: esconde num PNG o texto cifrado de um aviso e
 * extrai de volta. Restrito a ADMINISTRADOR, como o resto do Projeto 2.
 *
 * O que vai para a imagem e o valor da coluna avisos.conteudo como esta no
 * banco ("v1:" + Base64), lido por JDBC e nao pela entidade: pela entidade
 * o converter devolveria o texto ja decifrado. Assim a demonstracao mostra a
 * ordem certa - cifrar antes de esconder - com o proprio dado do SIGEA.
 *
 * Allowlist propria, como o brief exige: so PNG, pela assinatura do arquivo
 * (EsteganografiaLSB valida e recusa JPG com erro explicito). Este endpoint
 * nao passa pelo UploadController, que aceita jpg e gif para anexos, e nada
 * aqui e gravado em disco.
 */
@RestController
@RequestMapping("/api/admin/esteganografia")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class EsteganografiaController {

    static final int MAXIMO_TEXTO = 10_000;
    private static final int AMOSTRA_CIFRADO = 80;
    private static final int MAXIMO_AVISOS_LISTADOS = 200;

    private final CifraConteudo cifra;
    private final RegistradorLogSeguranca registrador;
    private final JdbcTemplate jdbc;

    public EsteganografiaController(CifraConteudo cifra, RegistradorLogSeguranca registrador, DataSource dataSource) {
        this.cifra = cifra;
        this.registrador = registrador;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public record AvisoResumido(long id, String titulo, boolean cifrado) {
    }

    @GetMapping("/avisos")
    public List<AvisoResumido> avisos() {
        return jdbc.query("SELECT aviso_id, titulo, conteudo FROM avisos ORDER BY aviso_id DESC FETCH FIRST "
                        + MAXIMO_AVISOS_LISTADOS + " ROWS ONLY",
                (rs, n) -> new AvisoResumido(rs.getLong(1), rs.getString(2), cifra.estaCifrado(rs.getString(3))));
    }

    @PostMapping("/embutir")
    public ResponseEntity<Map<String, Object>> embutir(@RequestParam("imagem") MultipartFile imagem,
                                                       @RequestParam(value = "avisoId", required = false) Long avisoId,
                                                       @RequestParam(value = "texto", required = false) String texto,
                                                       Authentication autenticacao, HttpServletRequest requisicao)
            throws IOException {
        boolean temTexto = texto != null && !texto.isBlank();
        if ((avisoId == null) == !temTexto) {
            return erro(HttpStatus.BAD_REQUEST, "Informe um aviso ou um texto, nao os dois.");
        }
        if (temTexto && texto.length() > MAXIMO_TEXTO) {
            return erro(HttpStatus.BAD_REQUEST, "Texto acima de " + MAXIMO_TEXTO + " caracteres.");
        }

        String recurso;
        String textoCifrado;
        if (avisoId != null) {
            recurso = "avisos/" + avisoId;
            List<String> conteudo = jdbc.queryForList("SELECT conteudo FROM avisos WHERE aviso_id = ?", String.class, avisoId);
            if (conteudo.isEmpty()) {
                return erro(HttpStatus.NOT_FOUND, "Aviso " + avisoId + " nao encontrado.");
            }
            textoCifrado = conteudo.get(0);
            if (!cifra.estaCifrado(textoCifrado)) {
                // Legado ainda em claro (o runner de recifragem nao rodou):
                // cifra agora, para nunca esconder texto claro na imagem.
                textoCifrado = cifra.cifrar("avisos.conteudo", textoCifrado);
            }
        } else {
            recurso = "esteganografia/texto";
            textoCifrado = cifra.cifrar(recurso, texto);
        }

        byte[] original = imagem.getBytes();
        byte[] payload = textoCifrado.getBytes(StandardCharsets.US_ASCII);
        byte[] estego;
        try {
            estego = EsteganografiaLSB.embutir(original, payload);
        } catch (EsteganografiaLSB.FalhaEsteganografia e) {
            registrar(TipoEventoSeguranca.ESTEGO_EMBUTIR, autenticacao, recurso, requisicao, ResultadoEvento.FALHA,
                    "{\"payload_bytes\":" + payload.length + "}");
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        int[] pixelsOriginal = EsteganografiaLSB.pixels(original);
        int[] pixelsEstego = EsteganografiaLSB.pixels(estego);
        double psnr = AnaliseEsteganografia.psnr(pixelsOriginal, pixelsEstego);
        int capacidade = EsteganografiaLSB.capacidade(estego);
        // O qui-quadrado olha so a regiao que o payload ocupou (cabecalho +
        // payload, em valores de canal): o embutimento e sequencial, e e ali
        // que a assinatura aparece. Na imagem inteira ela se dilui.
        long valoresUsados = (long) (EsteganografiaLSB.TAMANHO_CABECALHO + payload.length) * 8;
        ResultadoQuiQuadrado chiOriginal = AnaliseEsteganografia.quiQuadrado(pixelsOriginal, valoresUsados);
        ResultadoQuiQuadrado chiEstego = AnaliseEsteganografia.quiQuadrado(pixelsEstego, valoresUsados);

        registrar(TipoEventoSeguranca.ESTEGO_EMBUTIR, autenticacao, recurso, requisicao, ResultadoEvento.SUCESSO,
                "{\"payload_bytes\":" + payload.length + ",\"psnr_db\":" + formatar(psnr) + "}");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("imagemPng", Base64.getEncoder().encodeToString(estego));
        corpo.put("payloadBytes", payload.length);
        corpo.put("capacidadeBytes", capacidade);
        corpo.put("capacidadeUsadaPct", capacidade == 0 ? 0 : 100.0 * payload.length / capacidade);
        corpo.put("psnrDb", finito(psnr));
        corpo.put("pValorOriginal", finito(chiOriginal.pValor()));
        corpo.put("pValorEstego", finito(chiEstego.pValor()));
        corpo.put("textoCifradoInicio", amostra(textoCifrado));
        return ResponseEntity.ok(corpo);
    }

    @PostMapping("/extrair")
    public ResponseEntity<Map<String, Object>> extrair(@RequestParam("imagem") MultipartFile imagem,
                                                       Authentication autenticacao, HttpServletRequest requisicao)
            throws IOException {
        byte[] payload;
        try {
            payload = EsteganografiaLSB.extrair(imagem.getBytes());
        } catch (EsteganografiaLSB.FalhaEsteganografia e) {
            registrar(TipoEventoSeguranca.ESTEGO_EXTRAIR, autenticacao, "esteganografia/extrair", requisicao,
                    ResultadoEvento.FALHA, "{\"etapa\":\"lsb\"}");
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        String textoCifrado = new String(payload, StandardCharsets.US_ASCII);
        registrar(TipoEventoSeguranca.ESTEGO_EXTRAIR, autenticacao, "esteganografia/extrair", requisicao,
                ResultadoEvento.SUCESSO, "{\"payload_bytes\":" + payload.length + "}");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("payloadBytes", payload.length);
        corpo.put("textoCifradoInicio", amostra(textoCifrado));
        try {
            corpo.put("textoClaro", cifra.decifrar("esteganografia/extrair", textoCifrado));
            return ResponseEntity.ok(corpo);
        } catch (CifraSigea.FalhaDecifragem e) {
            // Imagem sem payload, payload adulterado ou cifrado com outra
            // chave. Sem autenticacao (nem no LSB nem no CBC), o que se
            // detecta e so o sintoma: formato ou padding invalido.
            corpo.put("erro", "Os bits extraidos nao decifram: " + e.getMessage());
            return ResponseEntity.unprocessableEntity().body(corpo);
        }
    }

    private void registrar(TipoEventoSeguranca tipo, Authentication autenticacao, String recurso,
                           HttpServletRequest requisicao, ResultadoEvento resultado, String detalhe) {
        try {
            registrador.registrar(new NovoEventoSeguranca(tipo, idDoAdmin(autenticacao), recurso,
                    requisicao.getRemoteAddr(), resultado, detalhe));
        } catch (RuntimeException e) {
            // A operacao vale mais que o registro dela.
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Falha ao registrar {}", tipo, e);
        }
    }

    private static String amostra(String texto) {
        return texto.length() <= AMOSTRA_CIFRADO ? texto : texto.substring(0, AMOSTRA_CIFRADO) + "...";
    }

    /** NaN e infinito nao existem em JSON; o Jackson os escreveria assim mesmo e o navegador recusaria. */
    private static Double finito(double valor) {
        return Double.isFinite(valor) ? valor : null;
    }

    private static String formatar(double valor) {
        return Double.isFinite(valor) ? String.format(java.util.Locale.ROOT, "%.2f", valor) : "null";
    }

    private static Long idDoAdmin(Authentication autenticacao) {
        try {
            return autenticacao == null ? null : Long.valueOf(autenticacao.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
