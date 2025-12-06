package dev.com.sigea.infraestrutura.repositorio;

import dev.com.sigea.dominio.usuario.*;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    
    private final UsuarioJpaRepository jpaRepository;
    
    public UsuarioRepositoryImpl(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void salvar(Usuario usuario) {
        UsuarioEntity entity = toEntity(usuario);
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(this::toDomain);
    }
    
    @Override
    public UsuarioId proximoId() {
        return new UsuarioId(UUID.randomUUID().toString());
    }
    
    private UsuarioEntity toEntity(Usuario usuario) {
        return new UsuarioEntity(
            null, 
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getSenha().getSenhaHash(),
            usuario.getPerfil().name(),
            usuario.getStatus().name()
        );
    }
    
    private Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(
            new UsuarioId(String.valueOf(entity.getId())),
            entity.getNome(),
            entity.getEmail(),
            new Senha(entity.getSenhaHash()),
            Perfil.valueOf(entity.getPerfil())
        );
    }
}
