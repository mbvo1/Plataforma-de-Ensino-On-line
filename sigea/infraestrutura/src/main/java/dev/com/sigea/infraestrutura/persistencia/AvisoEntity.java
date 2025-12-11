package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "avisos")
public class AvisoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aviso_id")
    private Long id;
    
    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;
    
    @Column(name = "conteudo", columnDefinition = "TEXT", nullable = false)
    private String conteudo;
    
    @Column(name = "autor_id", nullable = false)
    private Long autorId;
    
    @Column(name = "alvo_tipo", nullable = false, length = 50)
    private String alvoTipo;
    
    protected AvisoEntity() {
    }
    
    public AvisoEntity(String titulo, String conteudo, Long autorId, String alvoTipo) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autorId = autorId;
        this.alvoTipo = alvoTipo;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getConteudo() { return conteudo; }
    public Long getAutorId() { return autorId; }
    public String getAlvoTipo() { return alvoTipo; }
}
