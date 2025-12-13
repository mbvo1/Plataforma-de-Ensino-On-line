package dev.com.sigea.apresentacao.dashboard;

import dev.com.sigea.apresentacao.dashboard.dto.DashboardAlunoResponse;
import dev.com.sigea.apresentacao.dashboard.dto.DashboardStatsResponse;
import dev.com.sigea.apresentacao.dashboard.dto.UsuarioResumo;
import dev.com.sigea.infraestrutura.persistencia.*;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorEntity;
import dev.com.sigea.infraestrutura.persistencia.eventos.EventoProfessorJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final UsuarioJpaRepository usuarioRepository;
    private final DisciplinaJpaRepository disciplinaRepository;
    private final TurmaJpaRepository turmaRepository;
    private final MatriculaJpaRepository matriculaRepository;
    private final ChamadaJpaRepository chamadaRepository;
    private final AvaliacaoJpaRepository avaliacaoRepository;
    private final AvisoJpaRepository avisoRepository;
    private final AvisoLeituraJpaRepository avisoLeituraRepository;
    private final EventoJpaRepository eventoRepository;
    private final EventoProfessorJpaRepository eventoProfessorRepository;
    private final AtividadeTurmaJpaRepository atividadeTurmaRepository;
    private final TurmaAlunoJpaRepository turmaAlunoRepository;
    private final SalaJpaRepository salaRepository;
    private final PeriodoLetivoJpaRepository periodoLetivoRepository;
    
    public DashboardController(UsuarioJpaRepository usuarioRepository, 
                             DisciplinaJpaRepository disciplinaRepository, 
                             TurmaJpaRepository turmaRepository,
                             MatriculaJpaRepository matriculaRepository,
                             ChamadaJpaRepository chamadaRepository,
                             AvaliacaoJpaRepository avaliacaoRepository,
                             AvisoJpaRepository avisoRepository,
                             AvisoLeituraJpaRepository avisoLeituraRepository,
                             EventoJpaRepository eventoRepository,
                             EventoProfessorJpaRepository eventoProfessorRepository,
                             AtividadeTurmaJpaRepository atividadeTurmaRepository,
                             TurmaAlunoJpaRepository turmaAlunoRepository,
                             SalaJpaRepository salaRepository,
                             PeriodoLetivoJpaRepository periodoLetivoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.chamadaRepository = chamadaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.avisoRepository = avisoRepository;
        this.avisoLeituraRepository = avisoLeituraRepository;
        this.eventoRepository = eventoRepository;
        this.eventoProfessorRepository = eventoProfessorRepository;
        this.atividadeTurmaRepository = atividadeTurmaRepository;
        this.turmaAlunoRepository = turmaAlunoRepository;
        this.salaRepository = salaRepository;
        this.periodoLetivoRepository = periodoLetivoRepository;
    }
    
    @GetMapping("/aluno/{usuarioId}")
    public ResponseEntity<DashboardAlunoResponse> getDashboardAluno(@PathVariable Long usuarioId) {
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(usuarioId);
        
        if (usuario.isEmpty() || !"ALUNO".equals(usuario.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        DashboardAlunoResponse response = new DashboardAlunoResponse();
        response.setNomeAluno(usuario.get().getNome());
        
        // 1. Buscar eventos dos próximos 7 dias
        List<DashboardAlunoResponse.EventoProximo> eventosProximos = buscarEventosProximos(usuarioId);
        response.setEventosProximos(eventosProximos);
        
        // 2. Contar avisos não lidos
        int avisosNaoLidos = contarAvisosNaoLidos(usuarioId);
        response.setAvisosNaoLidos(avisosNaoLidos);
        response.setTotalAvisos(avisosNaoLidos); // Por enquanto, só mostra não lidos
        
        // 3. Buscar notas atribuídas
        List<DashboardAlunoResponse.NotaResumo> notasResumo = buscarNotasResumo(usuarioId);
        response.setNotasResumo(notasResumo);
        
        // 4. Calcular frequência
        int totalFaltas = calcularTotalFaltas(usuarioId);
        double frequenciaPercentual = calcularFrequenciaPercentual(usuarioId, totalFaltas);
        response.setTotalFaltas(totalFaltas);
        response.setFrequenciaPercentual(frequenciaPercentual);
        
        return ResponseEntity.ok(response);
    }
    
    private List<DashboardAlunoResponse.EventoProximo> buscarEventosProximos(Long alunoId) {
        List<DashboardAlunoResponse.EventoProximo> eventos = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDate seteDiasDepois = hoje.plusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // 1. Buscar eventos institucionais (TODOS ou ALUNOS) dos próximos 7 dias
        List<EventoEntity> eventosInstitucionais = eventoRepository.findAll();
        eventosInstitucionais.stream()
            .filter(e -> {
                String tipo = e.getTipo() != null ? e.getTipo().toUpperCase() : "";
                return ("TODOS".equals(tipo) || "ALUNOS".equals(tipo));
            })
            .filter(e -> {
                if (e.getDataEvento() == null) return false;
                LocalDate dataEvento = e.getDataEvento().toLocalDate();
                return !dataEvento.isBefore(hoje) && !dataEvento.isAfter(seteDiasDepois);
            })
            .forEach(e -> {
                DashboardAlunoResponse.EventoProximo evento = new DashboardAlunoResponse.EventoProximo();
                evento.setTitulo(e.getTitulo());
                evento.setTipo("EVENTO");
                evento.setData(e.getDataEvento().toLocalDate().format(formatter));
                eventos.add(evento);
            });
        
        // 2. Buscar eventos de professores das disciplinas matriculadas
        List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
        Set<Long> professoresIds = new HashSet<>();
        Map<Long, String> professorDisciplinaMap = new HashMap<>();
        
        for (MatriculaEntity matricula : matriculas) {
            Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
            if (salaOpt.isPresent()) {
                SalaEntity sala = salaOpt.get();
                if (sala.getProfessorId() != null) {
                    professoresIds.add(sala.getProfessorId());
                    Optional<DisciplinaEntity> discOpt = disciplinaRepository.findById(sala.getDisciplinaId());
                    if (discOpt.isPresent()) {
                        professorDisciplinaMap.put(sala.getProfessorId(), discOpt.get().getNome());
                    }
                }
            }
        }
        
        for (Long professorId : professoresIds) {
            List<EventoProfessorEntity> eventosProfessor = eventoProfessorRepository.findByProfessorIdOrderByDataEventoDesc(professorId);
            eventosProfessor.stream()
                .filter(e -> {
                    if (e.getDataEvento() == null) return false;
                    LocalDate dataEvento = e.getDataEvento().toLocalDate();
                    return !dataEvento.isBefore(hoje) && !dataEvento.isAfter(seteDiasDepois);
                })
                .forEach(e -> {
                    DashboardAlunoResponse.EventoProximo evento = new DashboardAlunoResponse.EventoProximo();
                    evento.setTitulo(e.getTitulo());
                    evento.setTipo("EVENTO_PROFESSOR");
                    evento.setData(e.getDataEvento().toLocalDate().format(formatter));
                    evento.setDisciplinaNome(professorDisciplinaMap.get(professorId));
                    eventos.add(evento);
                });
        }
        
        // 3. Buscar atividades de turma com prazo nos próximos 7 dias
        List<TurmaAlunoEntity> turmasAluno = turmaAlunoRepository.findByAlunoId(alunoId);
        for (TurmaAlunoEntity turmaAluno : turmasAluno) {
            List<AtividadeTurmaEntity> atividades = atividadeTurmaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaAluno.getTurmaId());
            atividades.stream()
                .filter(a -> a.getPrazo() != null)
                .filter(a -> {
                    LocalDate prazo = a.getPrazo().toLocalDate();
                    return !prazo.isBefore(hoje) && !prazo.isAfter(seteDiasDepois);
                })
                .forEach(a -> {
                    DashboardAlunoResponse.EventoProximo evento = new DashboardAlunoResponse.EventoProximo();
                    evento.setTitulo(a.getTitulo());
                    evento.setTipo("ATIVIDADE_TURMA");
                    evento.setData(a.getPrazo().toLocalDate().format(formatter));
                    Optional<TurmaEntity> turmaOpt = turmaRepository.findById(a.getTurmaId());
                    if (turmaOpt.isPresent()) {
                        evento.setDisciplinaNome(turmaOpt.get().getTitulo());
                    }
                    eventos.add(evento);
                });
        }
        
        // Ordenar por data
        eventos.sort(Comparator.comparing(DashboardAlunoResponse.EventoProximo::getData));
        
        return eventos;
    }
    
    private int contarAvisosNaoLidos(Long alunoId) {
        List<Long> avisosLidos = avisoLeituraRepository.findAvisosLidosByUsuarioId(alunoId);
        List<AvisoEntity> todosAvisos = avisoRepository.findAll();
        
        int count = 0;
        for (AvisoEntity aviso : todosAvisos) {
            String alvoTipo = aviso.getAlvoTipo() != null ? aviso.getAlvoTipo().toUpperCase() : "GERAL";
            boolean visivel = "GERAL".equals(alvoTipo) || "TODOS".equals(alvoTipo) || "ALUNOS".equals(alvoTipo);
            
            if (visivel && !avisosLidos.contains(aviso.getId())) {
                count++;
            }
        }
        
        return count;
    }
    
    private List<DashboardAlunoResponse.NotaResumo> buscarNotasResumo(Long alunoId) {
        List<DashboardAlunoResponse.NotaResumo> notasResumo = new ArrayList<>();
        List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
        
        for (MatriculaEntity matricula : matriculas) {
            Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
            if (salaOpt.isEmpty()) continue;
            
            SalaEntity sala = salaOpt.get();
            Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
            if (disciplinaOpt.isEmpty()) continue;
            
            List<AvaliacaoEntity> avaliacoes = avaliacaoRepository.findByMatriculaId(matricula.getId());
            
            Double av1 = null;
            Double av2 = null;
            
            for (AvaliacaoEntity avaliacao : avaliacoes) {
                String nomeAvaliacao = avaliacao.getNomeAvaliacao();
                if ("AV1".equalsIgnoreCase(nomeAvaliacao)) {
                    av1 = avaliacao.getValor();
                } else if ("AV2".equalsIgnoreCase(nomeAvaliacao)) {
                    av2 = avaliacao.getValor();
                }
            }
            
            // Só adiciona se tiver pelo menos uma nota
            if (av1 != null || av2 != null) {
                DashboardAlunoResponse.NotaResumo nota = new DashboardAlunoResponse.NotaResumo();
                nota.setDisciplinaNome(disciplinaOpt.get().getNome());
                nota.setAv1(av1);
                nota.setAv2(av2);
                
                // Calcula média parcial
                if (av1 != null && av2 != null) {
                    nota.setMediaParcial((av1 + av2) / 2.0);
                } else if (av1 != null) {
                    nota.setMediaParcial(av1);
                } else if (av2 != null) {
                    nota.setMediaParcial(av2);
                }
                
                notasResumo.add(nota);
            }
        }
        
        return notasResumo;
    }
    
    private int calcularTotalFaltas(Long alunoId) {
        List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
        int totalFaltas = 0;
        for (MatriculaEntity matricula : matriculas) {
            totalFaltas += matricula.getTotalFaltas() != null ? matricula.getTotalFaltas() : 0;
        }
        return totalFaltas;
    }
    
    private double calcularFrequenciaPercentual(Long alunoId, int totalFaltas) {
        List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
        final LocalDate hoje = LocalDate.now(); // Final para uso na lambda
        
        int totalAulas = 0;
        for (MatriculaEntity matricula : matriculas) {
            // Busca a sala para obter o período letivo
            Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
            if (salaOpt.isEmpty()) continue;
            
            SalaEntity sala = salaOpt.get();
            
            // Busca o período letivo da sala
            LocalDate dataInicioPeriodo = null;
            LocalDate dataFimPeriodo = null;
            
            if (sala.getPeriodoLetivoId() != null) {
                Optional<PeriodoLetivoEntity> periodoOpt = periodoLetivoRepository.findById(sala.getPeriodoLetivoId());
                if (periodoOpt.isPresent()) {
                    PeriodoLetivoEntity periodo = periodoOpt.get();
                    dataInicioPeriodo = periodo.getDataInicio();
                    dataFimPeriodo = periodo.getDataFim();
                }
            }
            
            // Cria variáveis finais para uso na lambda
            final LocalDate dataInicioPeriodoFinal = dataInicioPeriodo;
            final LocalDate dataFimPeriodoFinal = dataFimPeriodo;
            
            // Busca todas as chamadas da matrícula
            List<ChamadaEntity> todasChamadas = chamadaRepository.findByMatriculaIdOrderByDataChamadaDesc(matricula.getId());
            
            // Usa um Set para garantir que cada data seja contada apenas uma vez
            // Mesmo que o professor tenha salvado/ajustado a chamada várias vezes, cada data conta apenas uma vez
            Set<LocalDate> datasChamadasUnicas = todasChamadas.stream()
                .map(ChamadaEntity::getDataChamada)
                .filter(dataChamada -> {
                    // Não conta chamadas futuras
                    if (dataChamada.isAfter(hoje)) {
                        return false;
                    }
                    
                    // Se houver período letivo definido, filtra por ele
                    if (dataInicioPeriodoFinal != null && dataFimPeriodoFinal != null) {
                        // Só conta chamadas dentro do período letivo
                        return !dataChamada.isBefore(dataInicioPeriodoFinal) && !dataChamada.isAfter(dataFimPeriodoFinal);
                    }
                    
                    // Se não houver período definido, conta todas até hoje
                    return true;
                })
                .collect(Collectors.toSet());
            
            // Cada data única representa 2 aulas (aula1 + aula2)
            totalAulas += datasChamadasUnicas.size() * 2;
        }
        
        if (totalAulas == 0) {
            return 100.0; // Se não houver aulas ainda, frequência é 100%
        }
        
        int aulasPresentes = totalAulas - totalFaltas;
        double frequencia = (aulasPresentes * 100.0) / totalAulas;
        return Math.max(0.0, Math.min(100.0, frequencia)); // Garante entre 0 e 100
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        List<UsuarioEntity> todosUsuarios = usuarioRepository.findAll();
        
        long totalAlunos = todosUsuarios.stream()
            .filter(u -> "ALUNO".equals(u.getPerfil()))
            .count();
            
        long totalProfessores = todosUsuarios.stream()
            .filter(u -> "PROFESSOR".equals(u.getPerfil()))
            .count();
        
        // Contar disciplinas ativas
        List<DisciplinaEntity> todasDisciplinas = disciplinaRepository.findAll();
        long totalDisciplinas = todasDisciplinas.stream()
            .filter(d -> "ATIVO".equals(d.getStatus()))
            .count();
        
        // Contar total de turmas criadas por todos os professores
        long totalTurmas = turmaRepository.count();
        
        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setTotalAlunos(totalAlunos);
        response.setTotalProfessores(totalProfessores);
        response.setTotalDisciplinas(totalDisciplinas);
        response.setTotalTurmas(totalTurmas);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ultimos-usuarios")
    public ResponseEntity<List<UsuarioResumo>> getUltimosUsuarios() {
        List<UsuarioEntity> todosUsuarios = usuarioRepository.findAll();
        
        // Pegar os últimos 5 usuários que não são administradores (ordenar por ID decrescente)
        List<UsuarioResumo> ultimos = todosUsuarios.stream()
            .filter(u -> !"ADMINISTRADOR".equals(u.getPerfil()))
            .sorted((u1, u2) -> Long.compare(u2.getId(), u1.getId()))
            .limit(5)
            .map(this::toResumo)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ultimos);
    }
    
    private UsuarioResumo toResumo(UsuarioEntity entity) {
        UsuarioResumo resumo = new UsuarioResumo();
        resumo.setNome(entity.getNome());
        resumo.setPerfil(entity.getPerfil());
        return resumo;
    }
}
