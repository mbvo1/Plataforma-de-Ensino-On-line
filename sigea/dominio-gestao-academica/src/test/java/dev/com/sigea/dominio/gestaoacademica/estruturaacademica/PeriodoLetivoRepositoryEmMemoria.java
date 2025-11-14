package dev.com.sigea.dominio.gestaoacademica.estruturaacademica;

import dev.com.sigea.dominio.gestaoacademica.periodoletivo.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class PeriodoLetivoRepositoryEmMemoria implements PeriodoLetivoRepository {
    private final Map<PeriodoLetivoId, PeriodoLetivo> periodos = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(PeriodoLetivo novoPeriodo) {
        periodos.put(novoPeriodo.getId(), novoPeriodo);
    }

    @Override
    public Optional<PeriodoLetivo> buscarPorIdentificador(String id) {
        return periodos.values().stream().filter(p -> p.getIdentificador().equals(id)).findFirst();
    }

    @Override
    public PeriodoLetivoId proximoId() {
        return new PeriodoLetivoId(String.valueOf(sequence.getAndIncrement()));
    }

    @Override
    public List<PeriodoLetivo> listarTodas() {
        return new ArrayList<>(periodos.values());
    }

    public int totalDePeriodos() {
        return periodos.size();
    }
}