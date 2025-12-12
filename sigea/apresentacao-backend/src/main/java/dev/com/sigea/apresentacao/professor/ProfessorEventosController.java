package dev.com.sigea.apresentacao.professor;

import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professor/eventos")
@CrossOrigin(origins = "*")
public class ProfessorEventosController {

    private final EventoProfessorJpaRepository repo;

    public ProfessorEventosController(EventoProfessorJpaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<EventoProfessorResponse>> listar(@RequestParam Long professorId) {
        List<EventoProfessorEntity> list = repo.findByProfessorIdOrderByDataEventoDesc(professorId);
        List<EventoProfessorResponse> resp = list.stream().map(e -> {
            EventoProfessorResponse r = new EventoProfessorResponse();
            r.setId(e.getId());
            r.setTitulo(e.getTitulo());
            r.setDescricao(e.getDescricao());
            r.setDataEvento(e.getDataEvento());
            r.setProfessorId(e.getProfessorId());
            return r;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<EventoProfessorResponse> criar(@RequestBody EventoProfessorRequest req) {
        EventoProfessorEntity e = new EventoProfessorEntity();
        e.setTitulo(req.getTitulo());
        e.setDescricao(req.getDescricao());
        e.setDataEvento(req.getDataEvento());
        e.setProfessorId(req.getProfessorId());
        e.setCriadoEm(LocalDateTime.now());
        EventoProfessorEntity saved = repo.save(e);

        EventoProfessorResponse r = new EventoProfessorResponse();
        r.setId(saved.getId());
        r.setTitulo(saved.getTitulo());
        r.setDescricao(saved.getDescricao());
        r.setDataEvento(saved.getDataEvento());
        r.setProfessorId(saved.getProfessorId());

        return ResponseEntity.ok(r);
    }

    // DTOs
    public static class EventoProfessorRequest {
        private String titulo;
        private String descricao;
        private java.time.LocalDateTime dataEvento;
        private Long professorId;
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public java.time.LocalDateTime getDataEvento() { return dataEvento; }
        public void setDataEvento(java.time.LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
        public Long getProfessorId() { return professorId; }
        public void setProfessorId(Long professorId) { this.professorId = professorId; }
    }

    public static class EventoProfessorResponse {
        private Long id;
        private String titulo;
        private String descricao;
        private java.time.LocalDateTime dataEvento;
        private Long professorId;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public java.time.LocalDateTime getDataEvento() { return dataEvento; }
        public void setDataEvento(java.time.LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
        public Long getProfessorId() { return professorId; }
        public void setProfessorId(Long professorId) { this.professorId = professorId; }
    }
}
