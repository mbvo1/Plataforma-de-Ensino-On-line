package dev.com.sigea.dominio.gestaoacademica.disciplina;
import java.util.Optional;
public interface DisciplinaRepository {
    void salvar(Disciplina disciplina);
    Optional<Disciplina> buscarPorNome(String nome);
    DisciplinaId proximoId();
}