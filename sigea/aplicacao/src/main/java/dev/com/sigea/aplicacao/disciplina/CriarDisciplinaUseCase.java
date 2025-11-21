package dev.com.sigea.aplicacao.disciplina;

import dev.com.sigea.dominio.gestaoacademica.disciplina.Disciplina;
import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaId;
import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaRepository;

public class CriarDisciplinaUseCase {
    
    private final DisciplinaRepository disciplinaRepository;
    
    public CriarDisciplinaUseCase(DisciplinaRepository disciplinaRepository) {
        this.disciplinaRepository = disciplinaRepository;
    }
    
    public DisciplinaResumo executar(CriarDisciplinaCommand command) {
        DisciplinaId novoId = disciplinaRepository.proximoId();
        Disciplina novaDisciplina = new Disciplina(novoId, command.getNome());
        
        disciplinaRepository.salvar(novaDisciplina);
        
        return new DisciplinaResumo(
            Long.parseLong(novaDisciplina.getId().valor()),
            command.getCodigoDisciplina(),
            novaDisciplina.getNome(),
            command.getDescricao()
        );
    }
}

