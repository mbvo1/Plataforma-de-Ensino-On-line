package dev.com.sigea.apresentacao.eventos;

import dev.com.sigea.infraestrutura.persistencia.eventos.EventoEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventosController {

    private final EventoJpaRepository eventoJpaRepository;

    public EventosController(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * GET /api/eventos - Listar todos os eventos
     */
    @GetMapping
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        List<EventoEntity> entities = eventoJpaRepository.findAll();
        List<EventoResponse> response = new ArrayList<>();
        
        entities.forEach(entity -> {
            EventoResponse resp = new EventoResponse();
            resp.setId(entity.getId());
            resp.setTitulo(entity.getTitulo());
            resp.setDataEvento(entity.getDataEvento());
            resp.setResponsavelId(entity.getResponsavelId());
            resp.setTipo(entity.getTipo());
            response.add(resp);
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/eventos - Criar novo evento
     */
    @PostMapping
    public ResponseEntity<EventoResponse> criar(@RequestBody EventoRequest request) {
        // Cria entidade
        EventoEntity entity = new EventoEntity();
        entity.setTitulo(request.getTitulo());
        entity.setDataEvento(request.getDataEvento());
        entity.setResponsavelId(request.getResponsavelId());
        entity.setTipo(request.getTipo());
        
        // Salva no banco
        EventoEntity saved = eventoJpaRepository.save(entity);
        
        // Monta response
        EventoResponse response = new EventoResponse();
        response.setId(saved.getId());
        response.setTitulo(saved.getTitulo());
        response.setDataEvento(saved.getDataEvento());
        response.setResponsavelId(saved.getResponsavelId());
        response.setTipo(saved.getTipo());
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/eventos/{id} - Excluir evento
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!eventoJpaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        eventoJpaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // DTOs
    public static class EventoRequest {
        private String titulo;
        private LocalDateTime dataEvento;
        private Long responsavelId;
        private String tipo;

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

    public static class EventoResponse {
        private Long id;
        private String titulo;
        private LocalDateTime dataEvento;
        private Long responsavelId;
        private String tipo;

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
}
