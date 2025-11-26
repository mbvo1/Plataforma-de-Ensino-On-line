package dev.com.sigea.dominio.sala;

import java.util.List; 
import java.util.Optional;

public interface SalaRepository {
    Sala salvar(Sala sala);
    Optional<Sala> buscarPorId(SalaId id);
    SalaId proximoId();

    List<Sala> listarTodas();
}