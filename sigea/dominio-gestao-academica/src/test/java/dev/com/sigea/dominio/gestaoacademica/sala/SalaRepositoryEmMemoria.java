package dev.com.sigea.dominio.gestaoacademica.sala;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SalaRepositoryEmMemoria implements SalaRepository {
    private final Map<SalaId, Sala> salas = new HashMap<>();

    @Override
    public Sala salvar(Sala sala) {
        salas.put(sala.getId(), sala);
        return sala;
    }

    @Override
    public Optional<Sala> buscarPorId(SalaId id) {
        return Optional.ofNullable(salas.get(id));
    }

    @Override
    public SalaId proximoId() {
        return new SalaId(UUID.randomUUID().toString());
    }
}