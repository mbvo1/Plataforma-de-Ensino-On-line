package dev.com.sigea.dominio.forum;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TopicoRepositoryEmMemoria implements TopicoRepository {
    private final Map<TopicoId, Topico> topicos = new HashMap<>();

    @Override
    public void salvar(Topico topico) {
        topicos.put(topico.getId(), topico);
    }

    @Override
    public Optional<Topico> buscarPorId(TopicoId id) {
        return Optional.ofNullable(topicos.get(id));
    }

    @Override
    public TopicoId proximoId() {
        return new TopicoId(UUID.randomUUID().toString());
    }
}

