package dev.com.sigea.dominio.gestaoacademica.estruturaacademica;
import dev.com.sigea.dominio.gestaoacademica.disciplina.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
public class DisciplinaRepositoryEmMemoria implements DisciplinaRepository {
    private final Map<String, Disciplina> disciplinasPorNome = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public void salvar(Disciplina disciplina) {
        if (buscarPorNome(disciplina.getNome()).isPresent() && !buscarPorNome(disciplina.getNome()).get().getId().equals(disciplina.getId())) {
            throw new IllegalStateException("Já existe uma disciplina com este nome");
        }
        disciplinasPorNome.put(disciplina.getNome(), disciplina);
    }

    @Override
    public Optional<Disciplina> buscarPorNome(String nome) { return Optional.ofNullable(disciplinasPorNome.get(nome)); }
    @Override
    public DisciplinaId proximoId() { return new DisciplinaId(sequence.getAndIncrement()); }
    public int totalDeDisciplinas() { return disciplinasPorNome.size(); }
}