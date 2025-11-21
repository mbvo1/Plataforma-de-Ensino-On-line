package dev.com.sigea.dominio.identidadeacesso.usuario;

import java.util.Objects;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.Senha;

public class Usuario {

    private UsuarioId id;
    private String nome;
    private String email;
    private Senha senha;
    private Perfil perfil;
    private UsuarioStatus status;

    protected Usuario() {
    }

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
        this.senha = null; 
    }
    
    public Usuario(UsuarioId id, String nome, String email, String senhaPlana, Perfil perfil) {
        this(id, nome, email, perfil);
        this.senha = Senha.criar(senhaPlana);
    }
    
    public Usuario(UsuarioId id, String nome, String email, Senha senha, Perfil perfil) {
        this(id, nome, email, perfil);
        this.senha = Objects.requireNonNull(senha, "Senha não pode ser nula.");
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
    
    public Senha getSenha() {
        return senha;
    }
    
    public void definirSenha(String senhaPlana) {
        this.senha = Senha.criar(senhaPlana);
    }
    
    public boolean verificarSenha(String senhaPlana) {
        if (senha == null) {
            return false;
        }
        return senha.verificar(senhaPlana);
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