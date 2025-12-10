package dev.com.sigea.apresentacao.usuarios_admin.dto;

public class AlunoResponse {
    private Long id;
    private String nome;
    private String email;
    private String status;

    public AlunoResponse(Long id, String nome, String email, String status) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
