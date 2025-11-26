package dev.com.sigea.dominio.estruturaacademica;
import dev.com.sigea.dominio.disciplina.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
public class DisciplinaRepositoryEmMemoria implements DisciplinaRepository {
    private final Map<String, Disciplina> disciplinasPorNome = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(Disciplina disciplina) {
        disciplinasPorNome.put(disciplina.getNome(), disciplina);
    }

    @Override
    public Optional<Disciplina> buscarPorNome(String nome) { 
        return Optional.ofNullable(disciplinasPorNome.get(nome)); 
    }
    
    @Override
    public DisciplinaId proximoId() {
        return new DisciplinaId(String.valueOf(sequence.getAndIncrement()));
    }
    
    @Override
    public List<Disciplina> listarTodas() {
        return new ArrayList<>(disciplinasPorNome.values());
    }
    
    public int totalDeDisciplinas() { 
        return disciplinasPorNome.size(); 
    }
}

