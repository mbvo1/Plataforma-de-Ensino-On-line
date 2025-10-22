package dev.com.sigea.dominio.gestaoacademica.forum;

import java.util.Optional;

public interface TopicoRepository {
    void salvar(Topico topico);
    Optional<Topico> buscarPorId(TopicoId id);
    TopicoId proximoId();
}