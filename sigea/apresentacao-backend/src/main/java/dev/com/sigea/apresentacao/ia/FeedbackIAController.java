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

    // Mitigação 4 — Revisão humana: este endpoint agora APENAS GERA A
    // SUGESTÃO. Nada é salvo no Envio. A nota e o feedback só chegam à
    // aplicação de verdade se o professor confirmar explicitamente,
    // através do endpoint /confirmar-avaliacao abaixo.
    @PostMapping("/{atividadeId}/envios/{envioId}/feedback-ia")
    public ResponseEntity<?> sugerirFeedbackIA(@PathVariable Long atividadeId,
                                                @PathVariable Long envioId) throws Exception {

        Optional<EnvioAtividadeEntity> envioOpt = envioRepository.findById(envioId);
        if (envioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Envio não encontrado"));
        }

        EnvioAtividadeEntity envio = envioOpt.get();

        if (envio.getArquivoConteudo() == null || !envio.getArquivoConteudo().startsWith("data:")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Envio não possui arquivo em formato esperado"));
        }

        String base64 = envio.getArquivoConteudo().substring(envio.getArquivoConteudo().indexOf(",") + 1);
        byte[] bytesArquivo = java.util.Base64.getDecoder().decode(base64);

        String textoExtraido = ExtratorTextoPdf.extrairTexto(bytesArquivo);
        String respostaBrutaIA = clienteGeminiIA.gerarFeedback(textoExtraido);
        String textoFeedback = extrairTextoDaResposta(respostaBrutaIA);
        Double notaSugerida = extrairNotaDoTexto(textoFeedback);

        // NÃO persiste. Retorna só a sugestão, marcada claramente como tal.
        return ResponseEntity.ok(Map.of(
                "sugestaoFeedbackIA", textoFeedback,
                "sugestaoNotaIA", notaSugerida,
                "aviso", "Esta é uma sugestão gerada por IA. A nota só é aplicada após confirmação do professor."
        ));
    }

    // Mitigação 4, parte 2 — só esta ação, explícita e feita por um
    // professor, grava nota e feedback de verdade no Envio.
    @PostMapping("/{atividadeId}/envios/{envioId}/confirmar-avaliacao")
    public ResponseEntity<?> confirmarAvaliacao(@PathVariable Long atividadeId,
                                                 @PathVariable Long envioId,
                                                 @RequestBody Map<String, Object> corpo) {

        Optional<EnvioAtividadeEntity> envioOpt = envioRepository.findById(envioId);
        if (envioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Envio não encontrado"));
        }

        EnvioAtividadeEntity envio = envioOpt.get();

        Object notaObj = corpo.get("nota");
        Object feedbackObj = corpo.get("feedback");
        if (notaObj == null || feedbackObj == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Campos 'nota' e 'feedback' são obrigatórios"));
        }

        envio.setNota(Double.valueOf(notaObj.toString()));
        envio.setFeedbackProfessor(feedbackObj.toString());
        envioRepository.save(envio);

        return ResponseEntity.ok(Map.of("mensagem", "Avaliação confirmada e registrada pelo professor."));
    }

    private String extrairTextoDaResposta(String jsonBruto) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode raiz = mapper.readTree(jsonBruto);

        if (raiz.has("error")) {
            throw new RuntimeException("Erro retornado pela API do Gemini: " + raiz.path("error").path("message").asText());
        }

        return raiz.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private Double extrairNotaDoTexto(String texto) {
        Matcher m = Pattern.compile("\\d+([.,]\\d+)?").matcher(texto);
        if (m.find()) {
            return Double.parseDouble(m.group().replace(",", "."));
        }
        return null;
    }
}
