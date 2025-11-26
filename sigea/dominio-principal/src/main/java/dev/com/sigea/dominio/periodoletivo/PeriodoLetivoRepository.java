package dev.com.sigea.dominio.periodoletivo;
import java.util.List;
import java.util.Optional;
public interface PeriodoLetivoRepository {
    void salvar(PeriodoLetivo periodoLetivo);
    Optional<PeriodoLetivo> buscarPorIdentificador(String identificador);
    PeriodoLetivoId proximoId();
    List<PeriodoLetivo> listarTodas();
}