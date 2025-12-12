package dev.com.sigea.dominio.colaborativo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Avisos_Turma")
public class AvisoTurmaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aviso_turma_id")
    private Long id;
    
    @Column(name = "turma_id", nullable = false)
    private Long turmaId;
    
    @Column(name = "professor_id", nullable = false)
    private Long professorId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;
    
    @Column(name = "nome_arquivo")
    private String nomeArquivo;
    
    @Column(name = "data_postagem", nullable = false)
    private LocalDateTime dataPostagem;
    
    public AvisoTurmaEntity() {
    }
    
    public AvisoTurmaEntity(Long turmaId, Long professorId, String mensagem, String nomeArquivo) {
        this.turmaId = turmaId;
        this.professorId = professorId;
        this.mensagem = mensagem;
        this.nomeArquivo = nomeArquivo;
        this.dataPostagem = LocalDateTime.now();
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
    
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
    
    public LocalDateTime getDataPostagem() {
        return dataPostagem;
    }
    
    public void setDataPostagem(LocalDateTime dataPostagem) {
        this.dataPostagem = dataPostagem;
    }
}
