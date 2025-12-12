package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AtividadesTurma")
public class AtividadeTurmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atividade_id")
    private Long id;

    @Column(name = "turma_id", nullable = false)
    private Long turmaId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "arquivo_path")
    private String arquivoPath;

    @Column(name = "prazo", nullable = true)
    private LocalDateTime prazo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    public AtividadeTurmaEntity() {
        this.dataCriacao = LocalDateTime.now();
    }

    public AtividadeTurmaEntity(Long turmaId, Long professorId, String titulo, String descricao, String arquivoPath, LocalDateTime prazo) {
        this();
        this.turmaId = turmaId;
        this.professorId = professorId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.arquivoPath = arquivoPath;
        this.prazo = prazo;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }
    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getArquivoPath() { return arquivoPath; }
    public void setArquivoPath(String arquivoPath) { this.arquivoPath = arquivoPath; }
    public LocalDateTime getPrazo() { return prazo; }
    public void setPrazo(LocalDateTime prazo) { this.prazo = prazo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
