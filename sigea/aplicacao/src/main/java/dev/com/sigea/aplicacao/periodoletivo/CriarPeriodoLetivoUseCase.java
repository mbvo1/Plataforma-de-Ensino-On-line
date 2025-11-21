package dev.com.sigea.aplicacao.periodoletivo;

import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivo;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivoId;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivoRepository;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoStatus;

import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;

public class CriarPeriodoLetivoUseCase {
    
    private final PeriodoLetivoRepository periodoLetivoRepository;
    
    public CriarPeriodoLetivoUseCase(PeriodoLetivoRepository periodoLetivoRepository) {
        this.periodoLetivoRepository = periodoLetivoRepository;
    }
    
    public PeriodoLetivoResumo executar(CriarPeriodoLetivoCommand command) {
        PeriodoLetivoId novoId = periodoLetivoRepository.proximoId();
        
        Date dataInicio = Date.from(command.getDataInicio().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dataFim = Date.from(command.getDataFim().atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        PeriodoLetivo novoPeriodo = new PeriodoLetivo(
            novoId,
            command.getIdentificador(),
            dataInicio,
            dataFim
        );
        
        periodoLetivoRepository.salvar(novoPeriodo);
        
        LocalDate dataInicioLocal = novoPeriodo.getDataInicio().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dataFimLocal = novoPeriodo.getDataFim().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        
        return new PeriodoLetivoResumo(
            Long.parseLong(novoPeriodo.getId().valor()),
            novoPeriodo.getIdentificador(),
            dataInicioLocal,
            dataFimLocal,
            command.getDataInicioMatricula(),
            command.getDataFimMatricula(),
            novoPeriodo.getStatus().name()
        );
    }
}

