package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aviso_leituras")
public class AvisoLeituraEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leitura_id")
    private Long id;
    
    @Column(name = "aviso_id", nullable = false)
    private Long avisoId;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;
    
    protected AvisoLeituraEntity() {
    }
    
    public AvisoLeituraEntity(Long avisoId, Long usuarioId) {
        this.avisoId = avisoId;
        this.usuarioId = usuarioId;
        this.dataLeitura = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public Long getAvisoId() { return avisoId; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getDataLeitura() { return dataLeitura; }
}
