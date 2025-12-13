package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

/**
 * Entidade JPA para a tabela Avaliacoes.
 */
@Entity
@Table(name = "Avaliacoes")
public class AvaliacaoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avaliacao_id")
    private Long id;
    
    @Column(name = "matricula_id", nullable = false)
    private Long matriculaId;
    
    @Column(name = "nome_avaliacao", nullable = false)
    private String nomeAvaliacao;
    
    @Column(name = "valor")
    private Double valor;
    
    public AvaliacaoEntity() {
    }
    
    public AvaliacaoEntity(Long matriculaId, String nomeAvaliacao, Double valor) {
        this.matriculaId = matriculaId;
        this.nomeAvaliacao = nomeAvaliacao;
        this.valor = valor;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMatriculaId() {
        return matriculaId;
    }
    
    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }
    
    public String getNomeAvaliacao() {
        return nomeAvaliacao;
    }
    
    public void setNomeAvaliacao(String nomeAvaliacao) {
        this.nomeAvaliacao = nomeAvaliacao;
    }
    
    public Double getValor() {
        return valor;
    }
    
    public void setValor(Double valor) {
        this.valor = valor;
    }
}
