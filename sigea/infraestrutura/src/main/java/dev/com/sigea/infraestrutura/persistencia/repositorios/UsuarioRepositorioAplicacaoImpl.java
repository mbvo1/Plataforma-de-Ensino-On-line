package dev.com.sigea.infraestrutura.persistencia.repositorios;

import dev.com.sigea.aplicacao.usuario.UsuarioRepositorioAplicacao;
import dev.com.sigea.aplicacao.usuario.UsuarioResumo;
import dev.com.sigea.infraestrutura.persistencia.jpa.UsuarioJpa;
import dev.com.sigea.infraestrutura.persistencia.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UsuarioRepositorioAplicacaoImpl implements UsuarioRepositorioAplicacao {
    
    private final UsuarioJpaRepository jpaRepository;
    
    public UsuarioRepositorioAplicacaoImpl(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<UsuarioResumo> buscarResumoPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::paraResumo);
    }
    
    @Override
    public Optional<UsuarioResumo> buscarResumoPorEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(this::paraResumo);
    }
    
    @Override
    public List<UsuarioResumo> listarResumos() {
        return jpaRepository.findAll().stream()
            .map(this::paraResumo)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<UsuarioResumo> buscarPorPerfil(String perfil) {
        return jpaRepository.findAll().stream()
            .filter(jpa -> jpa.getPerfil().name().equals(perfil))
            .map(this::paraResumo)
            .collect(Collectors.toList());
    }
    
    private UsuarioResumo paraResumo(UsuarioJpa jpa) {
        return new UsuarioResumo(
            jpa.getId(),
            jpa.getNome(),
            jpa.getEmail(),
            jpa.getPerfil().name(),
            jpa.getStatus().name()
        );
    }
}

