package dev.com.sigea.apresentacao.disciplinas_periodos;

import dev.com.sigea.apresentacao.disciplinas_periodos.dto.*;
import dev.com.sigea.apresentacao.disciplinas_periodos.observer.*;
import dev.com.sigea.apresentacao.disciplinas_periodos.template.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Funcionalidade 08: Disciplinas/Períodos (Admin)
 * Padrões: Template Method (criação/ativação) + Observer (propagação)
 */
@RestController
@RequestMapping("/api/admin")
public class DisciplinasPeriodosController {
    
    private final Map<String, PeriodoResponse> periodos = new HashMap<>();
    private final List<PeriodoObserver> observers = new ArrayList<>();
    
    public DisciplinasPeriodosController() {
        observers.add(new ModuloMatriculasObserver());
        observers.add(new CalendarioObserver());
    }
    
    @PostMapping("/periodos")
    public ResponseEntity<PeriodoResponse> criarPeriodo(@RequestBody CriarPeriodoRequest request) {
        // Template Method Pattern: fluxo padronizado
        GestãoPeriodoTemplate template = new CriacaoPeriodoTemplate();
        String periodoId = template.criarPeriodo(
            request.getNome(),
            request.getDataInicio(),
            request.getDataFim()
        );
        
        PeriodoResponse periodo = new PeriodoResponse();
        periodo.setId(periodoId);
        periodo.setNome(request.getNome());
        periodo.setDataInicio(request.getDataInicio());
        periodo.setDataFim(request.getDataFim());
        periodo.setAtivo(false);
        
        periodos.put(periodoId, periodo);
        
        return ResponseEntity.ok(periodo);
    }
    
    @GetMapping("/periodos")
    public ResponseEntity<List<PeriodoResponse>> listarPeriodos() {
        return ResponseEntity.ok(new ArrayList<>(periodos.values()));
    }
    
    @PatchMapping("/periodos/{id}/ativar")
    public ResponseEntity<Void> ativarPeriodo(@PathVariable String id) {
        PeriodoResponse periodo = periodos.get(id);
        
        if (periodo != null) {
            // Desativa outros períodos
            periodos.values().forEach(p -> p.setAtivo(false));
            
            // Ativa o período selecionado
            periodo.setAtivo(true);
            
            // Observer Pattern: propaga mudança
            observers.forEach(obs -> obs.onPeriodoAtivado(id, periodo.getNome()));
        }
        
        return ResponseEntity.ok().build();
    }
}
