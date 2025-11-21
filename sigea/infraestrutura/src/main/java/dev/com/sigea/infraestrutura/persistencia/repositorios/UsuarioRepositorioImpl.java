package dev.com.sigea.infraestrutura.persistencia.repositorios;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.Senha;
import dev.com.sigea.dominio.identidadeacesso.usuario.Perfil;
import dev.com.sigea.dominio.identidadeacesso.usuario.Usuario;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioRepository;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioStatus;
import dev.com.sigea.infraestrutura.persistencia.jpa.UsuarioJpa;
import dev.com.sigea.infraestrutura.persistencia.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.Optional;

@Repository
public class UsuarioRepositorioImpl implements UsuarioRepository {
    
    private final UsuarioJpaRepository jpaRepository;
    
    public UsuarioRepositorioImpl(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void salvar(Usuario usuario) {
        Long id = usuario.getId() != null && !usuario.getId().valor().isEmpty() 
            ? Long.parseLong(usuario.getId().valor()) 
            : null;
        
        UsuarioJpa jpa = id != null && jpaRepository.existsById(id)
            ? jpaRepository.findById(id).orElse(new UsuarioJpa())
            : new UsuarioJpa();
        
        if (jpa.getId() == null && id != null) {
            jpa.setId(id);
        }
        jpa.setNome(usuario.getNome());
        jpa.setEmail(usuario.getEmail());
        if (usuario.getSenha() != null) {
            jpa.setSenhaHash(usuario.getSenha().getHash());
        }
        jpa.setPerfil(UsuarioJpa.PerfilJpa.valueOf(usuario.getPerfil().name()));
        jpa.setStatus(UsuarioJpa.UsuarioStatusJpa.valueOf(usuario.getStatus().name()));
        
        jpaRepository.save(jpa);
    }
    
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(this::paraDominio);
    }
    
    private Usuario paraDominio(UsuarioJpa jpa) {
        UsuarioId id = new UsuarioId(String.valueOf(jpa.getId()));
        Perfil perfil = Perfil.valueOf(jpa.getPerfil().name());
        UsuarioStatus status = UsuarioStatus.valueOf(jpa.getStatus().name());
        
        Usuario usuario = new Usuario(id, jpa.getNome(), jpa.getEmail(), perfil);
        
        if (jpa.getSenhaHash() != null) {
            try {
                Field senhaField = Usuario.class.getDeclaredField("senha");
                senhaField.setAccessible(true);
                senhaField.set(usuario, Senha.deHash(jpa.getSenhaHash()));
            } catch (Exception e) {
            }
        }
        
        if (status == UsuarioStatus.INATIVO && usuario.getStatus() == UsuarioStatus.ATIVO) {
            usuario.desativar();
        }
        return usuario;
    }
    
    @Override
    public UsuarioId proximoId() {
        return new UsuarioId(String.valueOf(System.currentTimeMillis()));
    }
}

