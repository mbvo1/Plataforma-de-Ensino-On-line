package dev.com.sigea.apresentacao.matricula;

import dev.com.sigea.apresentacao.matricula.dto.*;
import dev.com.sigea.apresentacao.matricula.iterator.SalasFiltradaIterator;
import dev.com.sigea.apresentacao.matricula.proxy.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Funcionalidade 06: Matrícula
 * Padrões: Proxy (validações) + Iterator (filtros paginados)
 */
@RestController
@RequestMapping("/api/matricula")
public class MatriculaController {
    
    private final MatriculaService matriculaService;
    
    public MatriculaController() {
        MatriculaServiceReal real = new MatriculaServiceReal();
        this.matriculaService = new MatriculaServiceProxy(real);
    }
    
    @GetMapping("/disciplinas-disponiveis")
    public ResponseEntity<List<SalaDisponivelResponse>> listarDisponiveis() {
        List<SalaDisponivelResponse> salas = new ArrayList<>();
        
        SalaDisponivelResponse sala = new SalaDisponivelResponse();
        sala.setId("SALA-001");
        sala.setDisciplinaNome("Engenharia de Software");
        sala.setProfessorNome("Prof. Silva");
        sala.setHorario("Seg/Qua 19h-21h");
        sala.setVagasDisponiveis(5);
        sala.setVagasTotal(40);
        salas.add(sala);
        
        // Iterator Pattern
        SalasFiltradaIterator iterator = new SalasFiltradaIterator(salas);
        List<SalaDisponivelResponse> resultado = new ArrayList<>();
        iterator.forEachRemaining(resultado::add);
        
        return ResponseEntity.ok(resultado);
    }
    
    @PostMapping("/salas/{id}")
    public ResponseEntity<Map<String, String>> matricular(
            @PathVariable String id, 
            @RequestBody MatricularRequest request) {
        
        // Proxy Pattern: valida período, vagas, horários
        matriculaService.matricular(id, request.getAlunoId());
        
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Matrícula realizada com sucesso");
        response.put("salaId", id);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        matriculaService.cancelar(id);
        return ResponseEntity.ok().build();
    }
}
