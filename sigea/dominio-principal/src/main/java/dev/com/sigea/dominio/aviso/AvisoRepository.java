package dev.com.sigea.dominio.aviso;

import java.util.Optional;

public interface AvisoRepository {
    void salvar(Aviso aviso);
    Optional<Aviso> buscarPorId(AvisoId id);
    AvisoId proximoId();
}