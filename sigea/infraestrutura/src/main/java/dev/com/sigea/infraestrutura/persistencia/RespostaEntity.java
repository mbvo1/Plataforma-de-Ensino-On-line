package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade JPA para a tabela Respostas.
 * Representa uma resposta/comentário em um tópico do fórum.
 */
@Entity
@Table(name = "Respostas")
public class RespostaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resposta_id")
    private Long id;
    
    @Column(name = "topico_id", nullable = false)
    private Long topicoId;
    
    @Column(name = "autor_id", nullable = false)
    private Long autorId;
    
    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;
    
    @Column(name = "verificada_pelo_professor", nullable = false)
    private Boolean verificadaPeloProfessor = false;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    public RespostaEntity() {
        this.dataCriacao = LocalDateTime.now();
    }
    
    public RespostaEntity(Long topicoId, Long autorId, String conteudo) {
        this.topicoId = topicoId;
        this.autorId = autorId;
        this.conteudo = conteudo;
        this.verificadaPeloProfessor = false;
        this.dataCriacao = LocalDateTime.now();
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTopicoId() {
        return topicoId;
    }
    
    public void setTopicoId(Long topicoId) {
        this.topicoId = topicoId;
    }
    
    public Long getAutorId() {
        return autorId;
    }
    
    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }
    
    public String getConteudo() {
        return conteudo;
    }
    
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public Boolean getVerificadaPeloProfessor() {
        return verificadaPeloProfessor;
    }
    
    public void setVerificadaPeloProfessor(Boolean verificadaPeloProfessor) {
        this.verificadaPeloProfessor = verificadaPeloProfessor;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
