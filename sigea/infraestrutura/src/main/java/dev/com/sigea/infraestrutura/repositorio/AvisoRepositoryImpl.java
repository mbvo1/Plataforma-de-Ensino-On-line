package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.aviso.Aviso;
import dev.com.sigea.dominio.aviso.AvisoId;
import dev.com.sigea.dominio.aviso.AvisoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AvisoRepositoryImpl implements AvisoRepository {
    
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
