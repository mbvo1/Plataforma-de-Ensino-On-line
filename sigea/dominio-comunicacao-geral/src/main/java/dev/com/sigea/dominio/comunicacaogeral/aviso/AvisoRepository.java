package dev.com.sigea.dominio.comunicacao.aviso;

import java.util.Optional;

public interface AvisoRepository {
    void salvar(Aviso aviso);
    Optional<Aviso> buscarPorId(AvisoId id);
    AvisoId proximoId();
}