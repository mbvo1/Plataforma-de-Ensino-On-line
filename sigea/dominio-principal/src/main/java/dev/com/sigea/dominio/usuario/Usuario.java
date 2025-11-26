package dev.com.sigea.dominio.usuario;

import java.util.Objects;

public class Usuario {

    private UsuarioId id;
    private String nome;
    private String email;
    private Perfil perfil;
    private UsuarioStatus status;

    public Usuario(UsuarioId id, String nome, String email, Perfil perfil) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        this.id = Objects.requireNonNull(id, "ID do usuário não pode ser nulo.");
        this.nome = nome;
        this.email = email;
        this.perfil = Objects.requireNonNull(perfil, "Perfil não pode ser nulo.");
        this.status = UsuarioStatus.ATIVO; 
    }

    public UsuarioId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public UsuarioStatus getStatus() {
        return status;
    }

    public void desativar() {
        if (this.status == UsuarioStatus.INATIVO) {
            throw new IllegalStateException("Usuário já está inativo.");
        }
        this.status = UsuarioStatus.INATIVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}