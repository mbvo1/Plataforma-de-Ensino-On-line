package dev.com.sigea.dominio.sala;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List; 
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

    @Override
    public List<Sala> listarTodas() {
        return new ArrayList<>(salas.values());
    }
}

