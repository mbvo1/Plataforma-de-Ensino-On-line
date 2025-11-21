package dev.com.sigea.aplicacao.periodoletivo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarPeriodoLetivoCommand {
    private String identificador;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataInicioMatricula;
    private LocalDate dataFimMatricula;
}

