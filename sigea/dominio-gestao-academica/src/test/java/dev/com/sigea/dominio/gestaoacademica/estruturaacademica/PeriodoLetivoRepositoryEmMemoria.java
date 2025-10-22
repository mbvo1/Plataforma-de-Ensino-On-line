package dev.com.sigea.dominio.gestaoacademica.estruturaacademica;

import dev.com.sigea.dominio.gestaoacademica.periodoletivo.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class PeriodoLetivoRepositoryEmMemoria implements PeriodoLetivoRepository {
    private final Map<PeriodoLetivoId, PeriodoLetivo> periodos = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(PeriodoLetivo novoPeriodo) {
        for (PeriodoLetivo existente : periodos.values()) {
            if (existente.getId().equals(novoPeriodo.getId())) continue;
            boolean sobrepoe = !novoPeriodo.getDataInicio().after(existente.getDataFim()) && !novoPeriodo.getDataFim().before(existente.getDataInicio());
            if (sobrepoe) {
                throw new IllegalStateException("Já existe um período letivo ativo nas datas especificadas");
            }
        }
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

    public int totalDePeriodos() {
        return periodos.size();
    }
}