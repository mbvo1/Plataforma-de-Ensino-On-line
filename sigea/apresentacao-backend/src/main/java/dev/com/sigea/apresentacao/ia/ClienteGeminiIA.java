package dev.com.sigea.apresentacao.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ClienteGeminiIA {

    @Value("${gemini.api.key}")
    private String chaveApi;

    private static final String MODELO = "gemini-3.5-flash-lite";
    private static final String URL_BASE =
        "https://generativelanguage.googleapis.com/v1beta/models/" + MODELO + ":generateContent";

    public String gerarFeedback(String textoTrabalho) throws Exception {

        // ATENÇÃO: esta concatenação direta, sem delimitação entre instrução
        // e conteúdo do aluno, É A VULNERABILIDADE PROPOSITAL desta v1.
        String prompt = "Avalie o trabalho do aluno abaixo e dê uma nota de 0 a 10, "
                + "com um breve comentário sobre a qualidade do conteúdo:\n\n"
                + textoTrabalho;

        String corpoRequisicao = """
            {
              "contents": [{
                "parts": [{ "text": "%s" }]
              }]
            }
            """.formatted(escaparJson(prompt));

        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", chaveApi)
                .POST(HttpRequest.BodyPublishers.ofString(corpoRequisicao))
                .build();

        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
        return resposta.body();
    }

    private String escaparJson(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}