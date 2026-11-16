package dev.com.sigea.dominio.cripto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gravacao dos CSVs de evidencia em sigea/evidencias/projeto-2/.
 *
 * Regra do brief: nenhum numero vai para o relatorio sem sair de um CSV
 * gerado por execucao real. Por isso os testes de medicao escrevem aqui e
 * nunca so imprimem. Publica porque os testes da Fase 4 (pacote auditoria)
 * gravam na mesma pasta. Locale.ROOT em toda formatacao: na JVM em pt-BR,
 * String.format escreveria "49,60" e a virgula decimal quebraria o CSV.
 */
public final class Evidencias {

    private Evidencias() {
    }

    public static Path diretorio() {
        // O surefire roda com o diretorio do modulo (dominio-principal) como cwd.
        Path dir = Paths.get(System.getProperty("evidencias.dir", "../evidencias/projeto-2"));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dir;
    }

    public static String fmt(String formato, Object... args) {
        return String.format(Locale.ROOT, formato, args);
    }

    /** Acumula linhas de um CSV e grava de uma vez, com cabecalho. */
    public static final class Csv {
        private final String nome;
        private final List<String> linhas = new ArrayList<>();

        public Csv(String nome, String cabecalho) {
            this.nome = nome;
            linhas.add(cabecalho);
        }

        public void linha(String formato, Object... args) {
            linhas.add(fmt(formato, args));
        }

        public int linhasDeDados() {
            return linhas.size() - 1;
        }

        public Path gravar() {
            Path arquivo = diretorio().resolve(nome);
            try {
                Files.write(arquivo, linhas, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            System.out.println("[Evidencias] " + arquivo.normalize() + " (" + linhasDeDados() + " linhas)");
            return arquivo;
        }
    }
}
