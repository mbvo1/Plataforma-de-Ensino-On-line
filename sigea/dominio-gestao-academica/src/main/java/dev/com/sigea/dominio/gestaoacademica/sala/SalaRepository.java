package dev.com.sigea.dominio.gestaoacademica.sala;

import java.util.Optional;

public interface SalaRepository {
    Sala salvar(Sala sala);
    Optional<Sala> buscarPorId(SalaId id);
    SalaId proximoId();
}