package dev.com.sigea.dominio.sala;

public record Nota(double valor) {
    public Nota {
        if (valor < 0.0 || valor > 10.0) {
            throw new IllegalArgumentException("Valor inválido. A nota deve ser um número entre 0 e 10.");
        }
    }
}