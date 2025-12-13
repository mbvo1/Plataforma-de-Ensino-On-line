package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Topicos")
public class TopicoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topico_id")
    private Long id;
    
    @Column(name = "disciplina_id", nullable = false)
    private Long disciplinaId;
    
    @Column(name = "autor_id", nullable = false)
    private Long autorId;
    
    @Column(name = "titulo", nullable = false)
    private String titulo;
    
    @Column(name = "conteudo", columnDefinition = "TEXT")
    private String conteudo;
    
    @Column(name = "arquivo_path")
    private String arquivoPath;
    
    // Constructors
    public TopicoEntity() {
    }
    
    public TopicoEntity(Long disciplinaId, Long autorId, String titulo, String conteudo, String arquivoPath) {
        this.disciplinaId = disciplinaId;
        this.autorId = autorId;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.arquivoPath = arquivoPath;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDisciplinaId() {
        return disciplinaId;
    }
    
    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }
    
    public Long getAutorId() {
        return autorId;
    }
    
    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getConteudo() {
        return conteudo;
    }
    
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public String getArquivoPath() {
        return arquivoPath;
    }
    
    public void setArquivoPath(String arquivoPath) {
        this.arquivoPath = arquivoPath;
    }
}
