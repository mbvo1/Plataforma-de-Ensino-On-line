package dev.com.sigea.dominio.aviso;

import dev.com.sigea.dominio.aviso.Aviso;
import dev.com.sigea.dominio.aviso.AvisoId;
import dev.com.sigea.dominio.aviso.AvisoRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AvisoRepositoryEmMemoria implements AvisoRepository {
    private final Map<AvisoId, Aviso> avisos = new HashMap<>();

    @Override
    public void salvar(Aviso aviso) {
        avisos.put(aviso.getId(), aviso);
    }

    @Override
    public Optional<Aviso> buscarPorId(AvisoId id) {
        return Optional.ofNullable(avisos.get(id));
    }

    @Override
    public AvisoId proximoId() {
        return new AvisoId(UUID.randomUUID().toString());
    }
}