package dev.com.sigea.dominio.cripto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Entradas de experimento lidas do proprio repositorio (corpus da Fase 2c,
 * capas PNG da Fase 3).
 *
 * git ls-files e nao uma varredura do disco: arquivo ignorado ou nao
 * versionado (relatorio gerado por ferramenta, rascunho local, imagem
 * baixada) mudaria a entrada de maquina para maquina, e o CSV deixaria de
 * ser reproduzivel.
 */
final class RepositorioGit {

    private RepositorioGit() {
    }

    /** Raiz do repositorio; o surefire roda com cwd em dominio-principal. */
    static Path raiz() {
        Path raiz = Paths.get(System.getProperty("repositorio.raiz", "../..")).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(raiz.resolve("README.md")), "Raiz do repositorio nao encontrada em " + raiz);
        return raiz;
    }

    /** Caminhos relativos a raiz, com barra normal, ordenados. */
    static List<String> arquivosVersionados(Path raiz) throws IOException {
        Process git = new ProcessBuilder("git", "ls-files", "-z").directory(raiz.toFile()).redirectErrorStream(true).start();
        String saida = new String(git.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int codigo = git.waitFor();
            assertEquals(0, codigo, "git ls-files falhou (git precisa estar no PATH): " + saida);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
        return Arrays.stream(saida.split("\0")).filter(s -> !s.isEmpty()).sorted().collect(Collectors.toList());
    }
}
