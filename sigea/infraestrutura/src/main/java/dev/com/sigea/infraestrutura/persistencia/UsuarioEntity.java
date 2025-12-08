package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "cpf", unique = true, length = 14)
    private String cpf;
    
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;
    
    @Column(name = "perfil", nullable = false, length = 50)
    private String perfil;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    protected UsuarioEntity() {
    }
    
    public UsuarioEntity(Long id, String nome, String email, String cpf, String senhaHash, String perfil, String status) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.status = status;
    }
    
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getSenhaHash() { return senhaHash; }
    public String getPerfil() { return perfil; }
    public String getStatus() { return status; }
    
    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public void setStatus(String status) { this.status = status; }
}
