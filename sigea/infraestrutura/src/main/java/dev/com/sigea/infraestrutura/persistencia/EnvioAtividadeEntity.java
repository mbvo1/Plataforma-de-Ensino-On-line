package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Envios")
public class EnvioAtividadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "envio_id")
    private Long id;

    @Column(name = "atividade_id", nullable = false)
    private Long atividadeId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "arquivo_path")
    private String arquivoPath;

    @Column(name = "arquivo_conteudo", columnDefinition = "LONGTEXT")
    private String arquivoConteudo;

    @Column(name = "feedback_professor", columnDefinition = "TEXT")
    private String feedbackProfessor;

    @Column(name = "nota")
    private Double nota;

    @Column(name = "status", nullable = false)
    private String status;

    public EnvioAtividadeEntity() {
        this.dataEnvio = LocalDateTime.now();
        this.status = "ENVIADO";
    }

    public EnvioAtividadeEntity(Long atividadeId, Long alunoId, String arquivoPath) {
        this();
        this.atividadeId = atividadeId;
        this.alunoId = alunoId;
        this.arquivoPath = arquivoPath;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAtividadeId() { return atividadeId; }
    public void setAtividadeId(Long atividadeId) { this.atividadeId = atividadeId; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public String getArquivoPath() { return arquivoPath; }
    public void setArquivoPath(String arquivoPath) { this.arquivoPath = arquivoPath; }

    public String getArquivoConteudo() { return arquivoConteudo; }
    public void setArquivoConteudo(String arquivoConteudo) { this.arquivoConteudo = arquivoConteudo; }

    public String getFeedbackProfessor() { return feedbackProfessor; }
    public void setFeedbackProfessor(String feedbackProfessor) { this.feedbackProfessor = feedbackProfessor; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
