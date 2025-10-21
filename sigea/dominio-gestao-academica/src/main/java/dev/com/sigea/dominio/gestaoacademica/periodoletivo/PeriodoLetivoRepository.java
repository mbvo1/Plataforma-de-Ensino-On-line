package dev.com.sigea.dominio.gestaoacademica.periodoletivo;
import java.util.Optional;
public interface PeriodoLetivoRepository {
    void salvar(PeriodoLetivo periodoLetivo);
    Optional<PeriodoLetivo> buscarPorIdentificador(String identificador);
    PeriodoLetivoId proximoId();
}