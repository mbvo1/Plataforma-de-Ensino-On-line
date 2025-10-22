package dev.com.sigea.dominio.comunicacaogeral.aviso;

import java.util.Optional;

public interface AvisoRepository {
    void salvar(Aviso aviso);
    Optional<Aviso> buscarPorId(AvisoId id);
    AvisoId proximoId();
}