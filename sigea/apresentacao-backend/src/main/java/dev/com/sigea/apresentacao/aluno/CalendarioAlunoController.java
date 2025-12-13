package dev.com.sigea.apresentacao.aluno;

import dev.com.sigea.infraestrutura.persistencia.*;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller REST para calendário do aluno.
 */
@RestController
@RequestMapping("/api/aluno")
@CrossOrigin(origins = "*")
public class CalendarioAlunoController {
    
    private final EventoJpaRepository eventoRepository;
    private final EventoProfessorJpaRepository eventoProfessorRepository;
    private final MatriculaJpaRepository matriculaRepository;
    private final SalaJpaRepository salaRepository;
    private final TurmaAlunoJpaRepository turmaAlunoRepository;
    private final AtividadeTurmaJpaRepository atividadeTurmaRepository;
    
    public CalendarioAlunoController(EventoJpaRepository eventoRepository,
                                    EventoProfessorJpaRepository eventoProfessorRepository,
                                    MatriculaJpaRepository matriculaRepository,
                                    SalaJpaRepository salaRepository,
                                    TurmaAlunoJpaRepository turmaAlunoRepository,
                                    AtividadeTurmaJpaRepository atividadeTurmaRepository) {
        this.eventoRepository = eventoRepository;
        this.eventoProfessorRepository = eventoProfessorRepository;
        this.matriculaRepository = matriculaRepository;
        this.salaRepository = salaRepository;
        this.turmaAlunoRepository = turmaAlunoRepository;
        this.atividadeTurmaRepository = atividadeTurmaRepository;
    }
    
    /**
     * GET /api/aluno/{alunoId}/eventos - Busca eventos visíveis para o aluno
     * Retorna:
     * - Eventos institucionais com tipo TODOS ou ALUNOS
     * - Eventos de professores das disciplinas que o aluno está matriculado
     */
    @GetMapping("/{alunoId}/eventos")
    public ResponseEntity<?> buscarEventosDoAluno(@PathVariable Long alunoId) {
        try {
            List<Map<String, Object>> eventosAluno = new ArrayList<>();
            
            // 1. Busca eventos institucionais (tipo TODOS ou ALUNOS)
            List<EventoEntity> eventosInstitucionais = eventoRepository.findAll();
            for (EventoEntity evento : eventosInstitucionais) {
                String tipo = evento.getTipo() != null ? evento.getTipo().toUpperCase() : "";
                if ("TODOS".equals(tipo) || "ALUNOS".equals(tipo)) {
                    Map<String, Object> eventoMap = new HashMap<>();
                    eventoMap.put("id", evento.getId());
                    eventoMap.put("titulo", evento.getTitulo());
                    eventoMap.put("data", evento.getDataEvento().toLocalDate().toString());
                    eventoMap.put("dataEvento", evento.getDataEvento().toString());
                    eventoMap.put("tipo", tipo);
                    eventoMap.put("responsavelId", evento.getResponsavelId());
                    eventosAluno.add(eventoMap);
                }
            }
            
            // 2. Busca matrículas ativas do aluno
            List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            
            // 3. Para cada matrícula, busca a sala e depois eventos do professor dessa sala
            Set<Long> professoresIds = new HashSet<>();
            for (MatriculaEntity matricula : matriculas) {
                Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
                if (salaOpt.isPresent()) {
                    SalaEntity sala = salaOpt.get();
                    professoresIds.add(sala.getProfessorId());
                }
            }
            
            // 4. Busca eventos de todos os professores das disciplinas do aluno
            for (Long professorId : professoresIds) {
                List<EventoProfessorEntity> eventosProfessor = eventoProfessorRepository.findByProfessorIdOrderByDataEventoDesc(professorId);
                for (EventoProfessorEntity eventoProf : eventosProfessor) {
                    Map<String, Object> eventoMap = new HashMap<>();
                    eventoMap.put("id", "prof_" + eventoProf.getId()); // Prefixo para identificar eventos de professor
                    eventoMap.put("titulo", eventoProf.getTitulo());
                    eventoMap.put("data", eventoProf.getDataEvento().toLocalDate().toString());
                    eventoMap.put("dataEvento", eventoProf.getDataEvento().toString());
                    eventoMap.put("tipo", "PROFESSOR");
                    eventoMap.put("professorId", eventoProf.getProfessorId());
                    eventosAluno.add(eventoMap);
                }
            }
            
            // 5. Busca atividades de turma do aluno
            List<TurmaAlunoEntity> turmasAluno = turmaAlunoRepository.findByAlunoId(alunoId);
            for (TurmaAlunoEntity turmaAluno : turmasAluno) {
                List<AtividadeTurmaEntity> atividades = atividadeTurmaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaAluno.getTurmaId());
                for (AtividadeTurmaEntity atividade : atividades) {
                    if (atividade.getPrazo() != null) {
                        Map<String, Object> eventoMap = new HashMap<>();
                        eventoMap.put("id", "atividade_" + atividade.getId());
                        eventoMap.put("titulo", atividade.getTitulo());
                        eventoMap.put("data", atividade.getPrazo().toLocalDate().toString());
                        eventoMap.put("dataEvento", atividade.getPrazo().toString());
                        eventoMap.put("tipo", "ATIVIDADE_TURMA");
                        eventosAluno.add(eventoMap);
                    }
                }
            }
            
            return ResponseEntity.ok(eventosAluno);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao buscar eventos: " + e.getMessage()));
        }
    }
}

