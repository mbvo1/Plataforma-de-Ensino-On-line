package dev.com.sigea.aplicacao.disciplina;

import java.util.List;
import java.util.Optional;

public interface DisciplinaRepositorioAplicacao {
    
    Optional<DisciplinaResumo> buscarResumoPorId(Long id);
    
    Optional<DisciplinaResumoExpandido> buscarResumoExpandidoPorId(Long id);
    
    List<DisciplinaResumo> listarResumos();
    
    List<DisciplinaResumo> buscarPorNome(String nome);
}

