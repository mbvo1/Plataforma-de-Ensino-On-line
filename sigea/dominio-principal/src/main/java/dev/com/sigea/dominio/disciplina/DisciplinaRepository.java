package dev.com.sigea.dominio.disciplina;
import java.util.List;
import java.util.Optional;
public interface DisciplinaRepository {
    void salvar(Disciplina disciplina);
    Optional<Disciplina> buscarPorNome(String nome);
    DisciplinaId proximoId();
    List<Disciplina> listarTodas();
}