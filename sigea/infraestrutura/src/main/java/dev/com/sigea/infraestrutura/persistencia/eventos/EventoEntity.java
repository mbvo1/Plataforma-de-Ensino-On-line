package dev.com.sigea.infraestrutura.persistencia.eventos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
public class EventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evento_id")
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    @Column(nullable = false, length = 50)
    private String tipo; // PROVA, ENTREGA_ATIVIDADE, FERIADO, PERIODO_MATRICULA

    // Constructors
    public EventoEntity() {}

    public EventoEntity(String titulo, LocalDateTime dataEvento, Long responsavelId, String tipo) {
        this.titulo = titulo;
        this.dataEvento = dataEvento;
        this.responsavelId = responsavelId;
        this.tipo = tipo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDateTime dataEvento) {
        this.dataEvento = dataEvento;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
