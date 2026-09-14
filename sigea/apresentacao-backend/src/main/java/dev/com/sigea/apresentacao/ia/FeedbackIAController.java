package dev.com.sigea.apresentacao.ia;

import dev.com.sigea.infraestrutura.persistencia.EnvioAtividadeEntity;
import dev.com.sigea.infraestrutura.persistencia.EnvioAtividadeJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/atividades")
public class FeedbackIAController {

    private final EnvioAtividadeJpaRepository envioRepository;
    private final ClienteGeminiIA clienteGeminiIA;

    public FeedbackIAController(EnvioAtividadeJpaRepository envioRepository,
                                 ClienteGeminiIA clienteGeminiIA) {
        this.envioRepository = envioRepository;
        this.clienteGeminiIA = clienteGeminiIA;
    }

    @PostMapping("/{atividadeId}/envios/{envioId}/feedback-ia")
    public ResponseEntity<?> gerarFeedbackIA(@PathVariable Long atividadeId,
                                              @PathVariable Long envioId) throws Exception {

        Optional<EnvioAtividadeEntity> envioOpt = envioRepository.findById(envioId);
        if (envioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Envio não encontrado"));
        }

        EnvioAtividadeEntity envio = envioOpt.get();

        if (envio.getArquivoConteudo() == null || !envio.getArquivoConteudo().startsWith("data:")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Envio não possui arquivo em formato esperado"));
        }

        // Extrai os bytes do PDF a partir do Base64 salvo no banco
        String base64 = envio.getArquivoConteudo().substring(envio.getArquivoConteudo().indexOf(",") + 1);
        byte[] bytesArquivo = java.util.Base64.getDecoder().decode(base64);

        String textoExtraido = ExtratorTextoPdf.extrairTexto(bytesArquivo);

        String respostaBrutaIA = clienteGeminiIA.gerarFeedback(textoExtraido);
        String textoFeedback = extrairTextoDaResposta(respostaBrutaIA);
        Double notaSugerida = extrairNotaDoTexto(textoFeedback);

        // ATENÇÃO: nesta v1 (proposital), a sugestão da IA é aplicada
        // diretamente, sem revisão do professor. Isso É a vulnerabilidade
        // a ser corrigida depois — a decisão final precisa ser humana.
        envio.setFeedbackProfessor(textoFeedback);
        envio.setNota(notaSugerida);
        envioRepository.save(envio);

        return ResponseEntity.ok(Map.of(
                "feedback", textoFeedback,
                "notaSugerida", notaSugerida
        ));
    }

    private String extrairTextoDaResposta(String jsonBruto) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode raiz = mapper.readTree(jsonBruto);
        return raiz.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private Double extrairNotaDoTexto(String texto) {
        // Heurística simples: procura o primeiro número no texto.
        // Não é confiável — o modelo pode responder em formatos variados.
        Matcher m = Pattern.compile("\\d+([.,]\\d+)?").matcher(texto);
        if (m.find()) {
            return Double.parseDouble(m.group().replace(",", "."));
        }
        return null;
    }
}