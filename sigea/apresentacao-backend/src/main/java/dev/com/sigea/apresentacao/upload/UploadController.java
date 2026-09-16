package dev.com.sigea.apresentacao.upload;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/";

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
        "pdf", "jpg", "jpeg", "png", "gif", "doc", "docx", "txt"
    );

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio"));
            }

            String originalFileName = file.getOriginalFilename();
            String extensao = extrairExtensao(originalFileName);

            if (extensao.isEmpty() || !EXTENSOES_PERMITIDAS.contains(extensao)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tipo de arquivo nao permitido. Extensoes aceitas: " + EXTENSOES_PERMITIDAS));
            }

            byte[] conteudo = file.getBytes();

            if (!conteudoCorrespondeExtensao(conteudo, extensao)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "O conteudo do arquivo nao corresponde ao tipo declarado (" + extensao + ")"));
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = UUID.randomUUID().toString() + "." + extensao;

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.write(filePath, conteudo);

            Map<String, String> response = new HashMap<>();
            response.put("filePath", UPLOAD_DIR + uniqueFileName);
            response.put("originalName", originalFileName);

            System.out.println("Arquivo salvo em: " + filePath.toAbsolutePath());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("Erro ao fazer upload: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao salvar arquivo"));
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }
        String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
        return extensao.replaceAll("[^a-z0-9]", "");
    }

    /**
     * Confere os magic bytes do conteudo contra o que a extensao declara.
     * Evita que um arquivo HTML/JS/script seja salvo com extensao de um
     * tipo inofensivo, ou servido depois com base num nome que nao reflete
     * o conteudo real.
     */
    private boolean conteudoCorrespondeExtensao(byte[] conteudo, String extensao) {
        return switch (extensao) {
            case "pdf" -> comecaCom(conteudo, "%PDF-".getBytes());
            case "jpg", "jpeg" -> comecaCom(conteudo, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case "png" -> comecaCom(conteudo, new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            });
            case "gif" -> comecaCom(conteudo, "GIF87a".getBytes()) || comecaCom(conteudo, "GIF89a".getBytes());
            case "docx" -> comecaCom(conteudo, new byte[]{0x50, 0x4B, 0x03, 0x04});
            case "doc" -> comecaCom(conteudo, new byte[]{
                (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
            });
            case "txt" -> conteudoPareceTextoPuro(conteudo);
            default -> false;
        };
    }

    private boolean comecaCom(byte[] conteudo, byte[] prefixo) {
        if (conteudo.length < prefixo.length) {
            return false;
        }
        for (int i = 0; i < prefixo.length; i++) {
            if (conteudo[i] != prefixo[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean conteudoPareceTextoPuro(byte[] conteudo) {
        int limite = Math.min(conteudo.length, 8192);
        for (int i = 0; i < limite; i++) {
            int b = conteudo[i] & 0xFF;
            boolean controleInvalido = b < 0x09 || (b > 0x0D && b < 0x20 && b != 0x1B);
            if (controleInvalido) {
                return false;
            }
        }
        return true;
    }
}
