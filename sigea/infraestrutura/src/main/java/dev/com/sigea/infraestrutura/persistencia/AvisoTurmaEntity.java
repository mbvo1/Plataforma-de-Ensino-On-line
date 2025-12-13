package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AvisosTurma")
public class AvisoTurmaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aviso_turma_id")
    private Long id;
    
    @Column(name = "turma_id", nullable = false)
    private Long turmaId;
    
    @Column(name = "professor_id", nullable = false)
    private Long professorId;
    
    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;
    
    @Column(name = "arquivo_path")
    private String arquivoPath;
    
    @Column(name = "arquivo_conteudo", columnDefinition = "LONGTEXT")
    private String arquivoConteudo;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    // Constructors
    public AvisoTurmaEntity() {
        this.dataCriacao = LocalDateTime.now();
    }
    
    public AvisoTurmaEntity(Long turmaId, Long professorId, String mensagem, String arquivoPath) {
        this();
        this.turmaId = turmaId;
        this.professorId = professorId;
        this.mensagem = mensagem;
        this.arquivoPath = arquivoPath;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTurmaId() {
        return turmaId;
    }
    
    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }
    
    public Long getProfessorId() {
        return professorId;
    }
    
    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public String getArquivoPath() {
        return arquivoPath;
    }
    
    public void setArquivoPath(String arquivoPath) {
        this.arquivoPath = arquivoPath;
    }
    
    public String getArquivoConteudo() {
        return arquivoConteudo;
    }
    
    public void setArquivoConteudo(String arquivoConteudo) {
        this.arquivoConteudo = arquivoConteudo;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
