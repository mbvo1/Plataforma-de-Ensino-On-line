package dev.com.sigea.apresentacao.disciplinas_periodos;

import dev.com.sigea.apresentacao.disciplinas_periodos.dto.*;
import dev.com.sigea.apresentacao.disciplinas_periodos.observer.*;
import dev.com.sigea.apresentacao.disciplinas_periodos.template.*;
import dev.com.sigea.infraestrutura.persistencia.PeriodoEntity;
import dev.com.sigea.infraestrutura.persistencia.PeriodoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.DisciplinaEntity;
import dev.com.sigea.infraestrutura.persistencia.DisciplinaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.SalaEntity;
import dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Funcionalidade 08: Disciplinas/Períodos (Admin)
 * Padrões: Template Method (criação/ativação) + Observer (propagação)
 */
@RestController
@RequestMapping("/api/admin")
public class DisciplinasPeriodosController {
    
    private final Map<String, PeriodoResponse> periodos = new HashMap<>();
    private final List<PeriodoObserver> observers = new ArrayList<>();
    private final PeriodoJpaRepository periodoJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final SalaJpaRepository salaJpaRepository;
    
    public DisciplinasPeriodosController(PeriodoJpaRepository periodoJpaRepository,
                                        DisciplinaJpaRepository disciplinaJpaRepository,
                                        SalaJpaRepository salaJpaRepository) {
        this.periodoJpaRepository = periodoJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.salaJpaRepository = salaJpaRepository;
        observers.add(new ModuloMatriculasObserver());
        observers.add(new CalendarioObserver());
    }
    
    @PostMapping("/periodos")
    public ResponseEntity<Map<String, Object>> criarPeriodo(@RequestBody CriarPeriodoRequest request) {
        try {
            // Verifica se já existe período ativo
            Optional<PeriodoEntity> periodoAtivoOpt = periodoJpaRepository.findByStatus("ATIVO");
            
            // Criar novo período
            PeriodoEntity novoPeriodo = new PeriodoEntity();
            novoPeriodo.setNome(request.getNome());
            novoPeriodo.setDataInicio(java.time.LocalDate.parse(request.getDataInicio()));
            novoPeriodo.setDataFim(java.time.LocalDate.parse(request.getDataFim()));
            novoPeriodo.setInscricaoInicio(java.time.LocalDate.parse(request.getInscricaoInicio()));
            novoPeriodo.setInscricaoFim(java.time.LocalDate.parse(request.getInscricaoFim()));
            novoPeriodo.setStatus("ATIVO");
            
            // Se existe período ativo, encerra ele
            if (periodoAtivoOpt.isPresent()) {
                PeriodoEntity periodoAntigo = periodoAtivoOpt.get();
                periodoAntigo.setStatus("ENCERRADO");
                periodoJpaRepository.save(periodoAntigo);
                
                // Salva o novo período
                PeriodoEntity periodoSalvo = periodoJpaRepository.save(novoPeriodo);
                
                // Busca todas as disciplinas do período anterior
                List<DisciplinaEntity> disciplinasAnteriores = disciplinaJpaRepository.findByPeriodoLetivoId(periodoAntigo.getId());
                
                // Desativa todas as disciplinas do período anterior
                for (DisciplinaEntity disciplina : disciplinasAnteriores) {
                    disciplina.setStatus("INATIVO");
                    disciplinaJpaRepository.save(disciplina);
                    
                    // Busca todas as salas dessa disciplina
                    List<SalaEntity> salasDaDisciplina = salaJpaRepository.findByDisciplinaId(disciplina.getId());
                    
                    // Desativa todas as salas da disciplina
                    for (SalaEntity sala : salasDaDisciplina) {
                        sala.setStatus("INATIVO");
                        salaJpaRepository.save(sala);
                    }
                }
                
                // Copia as disciplinas para o novo período como INATIVAS
                for (DisciplinaEntity disciplinaOriginal : disciplinasAnteriores) {
                    // Cria uma nova disciplina (cópia) para o novo período
                    DisciplinaEntity novaDisciplina = new DisciplinaEntity();
                    novaDisciplina.setCodigo(disciplinaOriginal.getCodigo() + "_" + periodoSalvo.getNome().replace(".", ""));
                    novaDisciplina.setNome(disciplinaOriginal.getNome());
                    novaDisciplina.setPeriodo(disciplinaOriginal.getPeriodo());
                    novaDisciplina.setStatus("INATIVO"); // Nova disciplina começa INATIVA
                    novaDisciplina.setPeriodoLetivo(periodoSalvo);
                    novaDisciplina.setSalasOfertadas(0);
                    
                    // Salva a nova disciplina
                    disciplinaJpaRepository.save(novaDisciplina);
                }
                
                // Observer Pattern: notifica encerramento do período antigo
                observers.forEach(obs -> obs.onPeriodoAtivado(periodoSalvo.getNome(), periodoSalvo.getNome()));
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", periodoSalvo.getId());
                response.put("nome", periodoSalvo.getNome());
                response.put("status", periodoSalvo.getStatus());
                response.put("dataInicio", periodoSalvo.getDataInicio().toString());
                response.put("dataFim", periodoSalvo.getDataFim().toString());
                response.put("inscricaoInicio", periodoSalvo.getInscricaoInicio().toString());
                response.put("inscricaoFim", periodoSalvo.getInscricaoFim().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Se não existe período anterior, apenas salva o novo
            PeriodoEntity periodoSalvo = periodoJpaRepository.save(novoPeriodo);
            
            // Desativa todas as disciplinas e salas existentes (caso existam sem período)
            List<DisciplinaEntity> todasDisciplinas = disciplinaJpaRepository.findAll();
            for (DisciplinaEntity disciplina : todasDisciplinas) {
                if (disciplina.getStatus() != null && disciplina.getStatus().equals("ATIVO")) {
                    disciplina.setStatus("INATIVO");
                    disciplinaJpaRepository.save(disciplina);
                    
                    // Desativa todas as salas dessa disciplina
                    List<SalaEntity> salasDaDisciplina = salaJpaRepository.findByDisciplinaId(disciplina.getId());
                    for (SalaEntity sala : salasDaDisciplina) {
                        if (sala.getStatus() != null && sala.getStatus().equals("ATIVO")) {
                            sala.setStatus("INATIVO");
                            salaJpaRepository.save(sala);
                        }
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", periodoSalvo.getId());
            response.put("nome", periodoSalvo.getNome());
            response.put("status", periodoSalvo.getStatus());
            response.put("dataInicio", periodoSalvo.getDataInicio().toString());
            response.put("dataFim", periodoSalvo.getDataFim().toString());
            response.put("inscricaoInicio", periodoSalvo.getInscricaoInicio().toString());
            response.put("inscricaoFim", periodoSalvo.getInscricaoFim().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao criar período letivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/periodos")
    public ResponseEntity<List<Map<String, Object>>> listarPeriodos() {
        List<PeriodoEntity> todosPeriodos = periodoJpaRepository.findAll();
        
        List<Map<String, Object>> periodosResponse = todosPeriodos.stream()
            .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())) // Mais recente primeiro
            .map(periodo -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", periodo.getId());
                map.put("nome", periodo.getNome());
                map.put("status", periodo.getStatus());
                map.put("dataInicio", periodo.getDataInicio() != null ? periodo.getDataInicio().toString() : null);
                map.put("dataFim", periodo.getDataFim() != null ? periodo.getDataFim().toString() : null);
                map.put("inscricaoInicio", periodo.getInscricaoInicio() != null ? periodo.getInscricaoInicio().toString() : null);
                map.put("inscricaoFim", periodo.getInscricaoFim() != null ? periodo.getInscricaoFim().toString() : null);
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(periodosResponse);
    }
    
    @GetMapping("/periodos/{id}/disciplinas")
    public ResponseEntity<List<Map<String, Object>>> listarDisciplinasPorPeriodo(@PathVariable Long id) {
        // Verifica se o período existe
        Optional<PeriodoEntity> periodoOpt = periodoJpaRepository.findById(id);
        if (periodoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Busca disciplinas do período específico
        List<DisciplinaEntity> disciplinas = disciplinaJpaRepository.findByPeriodoLetivoId(id);
        
        List<Map<String, Object>> disciplinasResponse = disciplinas.stream()
            .map(disciplina -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", disciplina.getId());
                map.put("codigo", disciplina.getCodigo());
                map.put("nome", disciplina.getNome());
                map.put("periodo", disciplina.getPeriodo());
                map.put("status", disciplina.getStatus());
                map.put("salasOfertadas", disciplina.getSalasOfertadas());
                map.put("preRequisitos", disciplina.getPreRequisitos().stream()
                    .map(pr -> Map.of("codigo", pr.getCodigo()))
                    .collect(Collectors.toList()));
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(disciplinasResponse);
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
