package dev.com.sigea.aplicacao.disciplina;

import java.util.List;
import java.util.Optional;

public class DisciplinaServicoAplicacao {
    
    private final DisciplinaRepositorioAplicacao repositorio;
    
    public DisciplinaServicoAplicacao(DisciplinaRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }
    
    public Optional<DisciplinaResumo> buscarPorId(Long id) {
        return repositorio.buscarResumoPorId(id);
    }
    
    public Optional<DisciplinaResumoExpandido> buscarExpandidoPorId(Long id) {
        return repositorio.buscarResumoExpandidoPorId(id);
    }
    
    public List<DisciplinaResumo> listarTodas() {
        return repositorio.listarResumos();
    }
    
    public List<DisciplinaResumo> buscarPorNome(String nome) {
        return repositorio.buscarPorNome(nome);
    }
}

