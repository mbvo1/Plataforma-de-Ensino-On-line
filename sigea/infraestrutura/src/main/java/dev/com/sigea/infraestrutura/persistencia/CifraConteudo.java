package dev.com.sigea.infraestrutura.persistencia;

import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.dominio.cripto.CifraSigea;
import dev.com.sigea.infraestrutura.auditoria.RegistradorLogSeguranca;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Projeto 2: a CifraSigea aplicada ao conteudo de avisos e topicos (decisao
 * do brief: "tipo de dado cifrado - texto"). Os dois converters JPA
 * delegam para ca, e a tela de esteganografia usa a mesma instancia.
 *
 * Chave: derivada do mesmo CPF_ENCRYPTION_KEY, com separacao de dominio, no
 * padrao do CpfCryptoConverter (SHA-256(segredo || ':' || finalidade)).
 * Uma variavel de ambiente nova obrigaria a cadastrar mais um segredo no
 * Railway, e esquecer dele derrubaria o deploy. O preco e conhecido: quem
 * tiver CPF_ENCRYPTION_KEY abre CPF e conteudo. Subchaves distintas evitam
 * o outro risco, o de usar a mesma chave crua em duas cifras diferentes.
 *
 * O AES-GCM do CPF continua sendo o baseline; esta cifra e o objeto de
 * estudo, e herda dele o contraste: CBC sem autenticacao. Trocar um byte
 * do texto cifrado no banco quase sempre decifra para lixo SEM erro - so o
 * padding invalido e detectado. Isso aparece no log: a adulteracao que
 * quebra o padding vira DECIFRAGEM_FALHA, a que nao quebra passa calada.
 * No CPF, o GCM recusaria as duas.
 *
 * Cada operacao grava CIFRAGEM_* ou DECIFRAGEM_* no log encadeado. O
 * registro e fail-open: uma falha no log nao impede de ler um aviso.
 */
@Component
public class CifraConteudo {

    /**
     * Devolvido no lugar do texto quando a decifragem falha. Lancar excecao
     * derrubaria a listagem inteira de avisos por causa de uma linha
     * adulterada; o texto fixo deixa o problema visivel sem esconder os outros.
     */
    public static final String CONTEUDO_INDISPONIVEL = "[conteudo indisponivel: falha na decifragem]";

    private static final Logger LOG = LoggerFactory.getLogger(CifraConteudo.class);
    private static final String PREFIXO = "v1:";

    private final CifraSigea cifra;
    // ObjectProvider e nao o registrador direto: os converters sao criados
    // pelo Hibernate durante o boot do EntityManagerFactory, e o registrador
    // depende do gerenciador de transacoes, que depende desse mesmo
    // EntityManagerFactory. Resolver na primeira gravacao quebra o ciclo.
    private final ObjectProvider<RegistradorLogSeguranca> registrador;

    public CifraConteudo(@Value("${cpf.encryption.key}") String segredo,
                         ObjectProvider<RegistradorLogSeguranca> registrador) {
        this.cifra = new CifraSigea(derivarChave(segredo));
        this.registrador = registrador;
    }

    public String cifrar(String recurso, String textoClaro) {
        if (textoClaro == null) {
            return null;
        }
        try {
            String cifrado = cifra.cifrarTexto(textoClaro);
            registrar(TipoEventoSeguranca.CIFRAGEM_OK, recurso, ResultadoEvento.SUCESSO,
                    "{\"cifra\":\"CifraSigea\",\"rodadas\":" + cifra.rodadas()
                            + ",\"bytes_claro\":" + textoClaro.getBytes(StandardCharsets.UTF_8).length + "}");
            return cifrado;
        } catch (RuntimeException e) {
            registrar(TipoEventoSeguranca.CIFRAGEM_FALHA, recurso, ResultadoEvento.FALHA,
                    "{\"motivo\":\"" + json(e.getClass().getSimpleName()) + "\"}");
            throw e;
        }
    }

    /** Para os converters: nunca lanca; texto sem prefixo e legado em claro. */
    public String decifrarOuIndisponivel(String recurso, String valorColuna) {
        if (valorColuna == null) {
            return null;
        }
        if (!valorColuna.startsWith(PREFIXO)) {
            // Linha gravada antes desta mudanca. O RecifragemConteudoLegado
            // cifra essas linhas na subida; ate la, continuam legiveis.
            return valorColuna;
        }
        try {
            return decifrar(recurso, valorColuna);
        } catch (CifraSigea.FalhaDecifragem e) {
            return CONTEUDO_INDISPONIVEL;
        }
    }

    /** Para a esteganografia: a falha volta para quem chamou, ja registrada. */
    public String decifrar(String recurso, String valorVersionado) {
        try {
            String claro = cifra.decifrarTexto(valorVersionado);
            registrar(TipoEventoSeguranca.DECIFRAGEM_OK, recurso, ResultadoEvento.SUCESSO,
                    "{\"cifra\":\"CifraSigea\",\"rodadas\":" + cifra.rodadas() + "}");
            return claro;
        } catch (CifraSigea.FalhaDecifragem e) {
            // A mensagem vem de um conjunto fixo da CifraSigea (prefixo,
            // Base64, tamanho, padding) e nunca carrega o texto cifrado.
            registrar(TipoEventoSeguranca.DECIFRAGEM_FALHA, recurso, ResultadoEvento.FALHA,
                    "{\"motivo\":\"" + json(e.getMessage()) + "\"}");
            throw e;
        }
    }

    public boolean estaCifrado(String valorColuna) {
        return valorColuna != null && valorColuna.startsWith(PREFIXO);
    }

    private void registrar(TipoEventoSeguranca tipo, String recurso, ResultadoEvento resultado, String detalhe) {
        try {
            RegistradorLogSeguranca r = registrador.getIfAvailable();
            if (r != null) {
                // usuario_id nulo: o converter roda dentro do Hibernate, sem
                // acesso ao usuario da requisicao (infraestrutura nao depende
                // do Spring Security). Quem fez a operacao aparece no log da
                // propria requisicao, quando houver.
                r.registrar(new NovoEventoSeguranca(tipo, null, recurso, null, resultado, detalhe));
            }
        } catch (RuntimeException e) {
            LOG.error("Falha ao registrar {} de {}", tipo, recurso, e);
        }
    }

    private static String json(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            if (c == '"' || c == '\\') {
                sb.append('\\').append(c);
            } else if (c >= 0x20) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static byte[] derivarChave(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException("cpf.encryption.key ausente: a chave do conteudo deriva dela");
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(segredo.getBytes(StandardCharsets.UTF_8));
            sha256.update((byte) ':');
            sha256.update("conteudo-cifra-sigea".getBytes(StandardCharsets.UTF_8));
            return sha256.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
        }
    }
}
