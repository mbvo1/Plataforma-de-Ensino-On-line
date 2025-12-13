package dev.com.sigea.apresentacao.aluno;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller REST para matrículas do aluno
 * 
 * Endpoints:
 * - GET /api/aluno/{alunoId}/matriculas - Listar disciplinas matriculadas
 * - GET /api/aluno/{alunoId}/disciplinas-disponiveis - Listar disciplinas disponíveis para matrícula
 * - GET /api/aluno/{alunoId}/disciplinas/{disciplinaId}/salas - Listar salas de uma disciplina
 * - POST /api/aluno/{alunoId}/matricular - Matricular em uma sala
 * - DELETE /api/aluno/{alunoId}/matriculas/{matriculaId} - Cancelar matrícula
 */
@RestController
@RequestMapping("/api/aluno")
@CrossOrigin(origins = "*")
public class AlunoMatriculaController {

    private final MatriculaJpaRepository matriculaRepository;
    private final SalaJpaRepository salaRepository;
    private final DisciplinaJpaRepository disciplinaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final PeriodoLetivoJpaRepository periodoLetivoRepository;

    public AlunoMatriculaController(MatriculaJpaRepository matriculaRepository,
                                     SalaJpaRepository salaRepository,
                                     DisciplinaJpaRepository disciplinaRepository,
                                     UsuarioJpaRepository usuarioRepository,
                                     PeriodoLetivoJpaRepository periodoLetivoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.salaRepository = salaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.periodoLetivoRepository = periodoLetivoRepository;
    }

    /**
     * GET /api/aluno/{alunoId}/matriculas - Listar disciplinas matriculadas
     */
    @GetMapping("/{alunoId}/matriculas")
    public ResponseEntity<?> listarMatriculas(@PathVariable Long alunoId) {
        try {
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aluno não encontrado"));
            }

            // Busca matrículas ativas do aluno
            List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (MatriculaEntity matricula : matriculas) {
                // Busca informações da sala
                Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
                if (salaOpt.isEmpty()) continue;
                
                SalaEntity sala = salaOpt.get();
                
                // Busca informações da disciplina
                Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
                if (disciplinaOpt.isEmpty()) continue;
                
                DisciplinaEntity disciplina = disciplinaOpt.get();
                
                // Busca nome do professor
                String nomeProfessor = usuarioRepository.findById(sala.getProfessorId())
                    .map(UsuarioEntity::getNome)
                    .orElse("Professor");
                
                // Busca período letivo
                String periodoLetivo = "";
                if (sala.getPeriodoLetivoId() != null) {
                    periodoLetivo = periodoLetivoRepository.findById(sala.getPeriodoLetivoId())
                        .map(PeriodoLetivoEntity::getIdentificador)
                        .orElse("");
                }
                
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("matriculaId", matricula.getId());
                item.put("salaId", sala.getId());
                item.put("salaIdentificador", sala.getIdentificador());
                item.put("disciplinaId", disciplina.getId());
                item.put("disciplinaCodigo", disciplina.getCodigo());
                item.put("disciplinaNome", disciplina.getNome());
                item.put("professorNome", nomeProfessor);
                item.put("horario", sala.getHorario());
                item.put("periodoLetivo", periodoLetivo);
                item.put("dataMatricula", matricula.getDataMatricula().format(formatter));
                item.put("situacao", matricula.getSituacao());
                item.put("totalFaltas", matricula.getTotalFaltas());
                
                resultado.add(item);
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar matrículas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/{alunoId}/disciplinas-disponiveis - Listar disciplinas disponíveis
     */
    @GetMapping("/{alunoId}/disciplinas-disponiveis")
    public ResponseEntity<?> listarDisciplinasDisponiveis(@PathVariable Long alunoId) {
        try {
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aluno não encontrado"));
            }

            // Busca matrículas ativas do aluno para saber quais disciplinas ele já está matriculado
            List<MatriculaEntity> matriculasAtivas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            Set<Long> disciplinasMatriculadas = new HashSet<>();
            
            for (MatriculaEntity mat : matriculasAtivas) {
                salaRepository.findById(mat.getSalaId())
                    .ifPresent(sala -> disciplinasMatriculadas.add(sala.getDisciplinaId()));
            }

            // Busca todas as disciplinas ativas
            List<DisciplinaEntity> todasDisciplinas = disciplinaRepository.findAll();
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (DisciplinaEntity disciplina : todasDisciplinas) {
                // Ignora disciplinas que o aluno já está matriculado
                if (disciplinasMatriculadas.contains(disciplina.getId())) {
                    continue;
                }
                
                // Verifica se há salas disponíveis para esta disciplina
                List<SalaEntity> salas = salaRepository.findByDisciplinaId(disciplina.getId());
                if (salas.isEmpty()) {
                    continue; // Não mostra disciplinas sem salas
                }
                
                // Conta vagas disponíveis
                int totalVagas = 0;
                int vagasOcupadas = 0;
                for (SalaEntity sala : salas) {
                    totalVagas += sala.getLimiteVagas();
                    vagasOcupadas += matriculaRepository.countBySalaIdAndStatus(sala.getId(), "ATIVA");
                }
                
                int vagasDisponiveis = totalVagas - (int) vagasOcupadas;
                
                if (vagasDisponiveis <= 0) {
                    continue; // Não mostra disciplinas sem vagas
                }
                
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("disciplinaId", disciplina.getId());
                item.put("codigo", disciplina.getCodigo());
                item.put("nome", disciplina.getNome());
                item.put("periodo", disciplina.getPeriodo());
                item.put("salasDisponiveis", salas.size());
                item.put("vagasDisponiveis", vagasDisponiveis);
                
                resultado.add(item);
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar disciplinas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/{alunoId}/disciplinas/{disciplinaId}/salas - Listar salas de uma disciplina
     */
    @GetMapping("/{alunoId}/disciplinas/{disciplinaId}/salas")
    public ResponseEntity<?> listarSalasDisciplina(
            @PathVariable Long alunoId,
            @PathVariable Long disciplinaId) {
        try {
            // Verifica se a disciplina existe
            Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(disciplinaId);
            if (disciplinaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Disciplina não encontrada"));
            }
            
            DisciplinaEntity disciplina = disciplinaOpt.get();
            
            // Busca a matrícula ativa do aluno para verificar em qual sala ele está matriculado
            List<MatriculaEntity> matriculasAtivas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            Set<Long> salasMatriculadas = new HashSet<>();
            for (MatriculaEntity mat : matriculasAtivas) {
                salasMatriculadas.add(mat.getSalaId());
            }
            
            // Busca salas da disciplina
            List<SalaEntity> salas = salaRepository.findByDisciplinaId(disciplinaId);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (SalaEntity sala : salas) {
                // Conta vagas ocupadas
                long vagasOcupadas = matriculaRepository.countBySalaIdAndStatus(sala.getId(), "ATIVA");
                int vagasDisponiveis = sala.getLimiteVagas() - (int) vagasOcupadas;
                
                // Verificar se o aluno está matriculado nesta sala
                boolean alunoMatriculadoNestaSala = salasMatriculadas.contains(sala.getId());
                
                // Mostra a sala se tem vagas OU se o aluno está matriculado nela
                if (vagasDisponiveis <= 0 && !alunoMatriculadoNestaSala) {
                    continue;
                }
                
                // Busca nome do professor
                String nomeProfessor = usuarioRepository.findById(sala.getProfessorId())
                    .map(UsuarioEntity::getNome)
                    .orElse("Professor");
                
                // Busca período letivo
                String periodoLetivo = "";
                if (sala.getPeriodoLetivoId() != null) {
                    periodoLetivo = periodoLetivoRepository.findById(sala.getPeriodoLetivoId())
                        .map(PeriodoLetivoEntity::getIdentificador)
                        .orElse("");
                }
                
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("salaId", sala.getId());
                item.put("identificador", sala.getIdentificador());
                item.put("professorNome", nomeProfessor);
                item.put("professorId", sala.getProfessorId());
                item.put("horario", sala.getHorario());
                item.put("periodoLetivo", periodoLetivo);
                item.put("limiteVagas", sala.getLimiteVagas());
                item.put("vagasOcupadas", vagasOcupadas);
                item.put("vagasDisponiveis", vagasDisponiveis);
                
                resultado.add(item);
            }
            
            // Retorna também info da disciplina
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("disciplinaId", disciplina.getId());
            response.put("disciplinaCodigo", disciplina.getCodigo());
            response.put("disciplinaNome", disciplina.getNome());
            response.put("salas", resultado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar salas: " + e.getMessage()));
        }
    }

    /**
     * POST /api/aluno/{alunoId}/matricular - Matricular em uma sala
     */
    @PostMapping("/{alunoId}/matricular")
    public ResponseEntity<?> matricular(
            @PathVariable Long alunoId,
            @RequestBody Map<String, Object> request) {
        try {
            Long salaId = ((Number) request.get("salaId")).longValue();
            
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aluno não encontrado"));
            }
            
            // Verifica se a sala existe
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Sala não encontrada"));
            }
            
            SalaEntity sala = salaOpt.get();
            
            // Busca matrículas ativas do aluno
            List<MatriculaEntity> matriculasAtivas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            
            // Verifica se o aluno já está matriculado nesta sala (entre as ativas)
            boolean jaMatriculadoNestaSala = matriculasAtivas.stream()
                .anyMatch(mat -> mat.getSalaId().equals(salaId));
            if (jaMatriculadoNestaSala) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Você já está matriculado nesta sala"));
            }
            
            // Verifica se o aluno já está matriculado em outra sala da mesma disciplina
            for (MatriculaEntity mat : matriculasAtivas) {
                Optional<SalaEntity> outraSala = salaRepository.findById(mat.getSalaId());
                if (outraSala.isPresent() && outraSala.get().getDisciplinaId().equals(sala.getDisciplinaId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Você já está matriculado em outra sala desta disciplina"));
                }
            }
            
            // Verifica se há vagas
            long vagasOcupadas = matriculaRepository.countBySalaIdAndStatus(salaId, "ATIVA");
            if (vagasOcupadas >= sala.getLimiteVagas()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Não há vagas disponíveis nesta sala"));
            }
            
            // Cria a matrícula
            MatriculaEntity matricula = new MatriculaEntity(salaId, alunoId);
            MatriculaEntity matriculaSalva = matriculaRepository.save(matricula);
            
            // Busca info da disciplina para retorno
            Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
            String disciplinaNome = disciplinaOpt.map(DisciplinaEntity::getNome).orElse("Disciplina");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Matrícula realizada com sucesso!",
                "matriculaId", matriculaSalva.getId(),
                "disciplinaNome", disciplinaNome,
                "salaIdentificador", sala.getIdentificador()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao realizar matrícula: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/aluno/{alunoId}/matriculas/{matriculaId} - Cancelar matrícula
     */
    @DeleteMapping("/{alunoId}/matriculas/{matriculaId}")
    public ResponseEntity<?> cancelarMatricula(
            @PathVariable Long alunoId,
            @PathVariable Long matriculaId) {
        try {
            // Busca a matrícula
            Optional<MatriculaEntity> matriculaOpt = matriculaRepository.findById(matriculaId);
            if (matriculaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Matrícula não encontrada"));
            }
            
            MatriculaEntity matricula = matriculaOpt.get();
            
            // Verifica se a matrícula pertence ao aluno
            if (!matricula.getAlunoId().equals(alunoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Esta matrícula não pertence a você"));
            }
            
            // Verifica se a matrícula está ativa
            if (!"ATIVA".equals(matricula.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Esta matrícula já foi cancelada"));
            }
            
            // Cancela a matrícula
            matricula.setStatus("CANCELADA");
            matriculaRepository.save(matricula);
            
            return ResponseEntity.ok(Map.of(
                "message", "Matrícula cancelada com sucesso"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao cancelar matrícula: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/{alunoId}/salas/{salaId}/info - Buscar informações de uma sala específica
     */
    @GetMapping("/{alunoId}/salas/{salaId}/info")
    public ResponseEntity<?> buscarInfoSala(
            @PathVariable Long alunoId,
            @PathVariable Long salaId) {
        try {
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Sala não encontrada"));
            }
            
            SalaEntity sala = salaOpt.get();
            
            // Buscar disciplina
            Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
            DisciplinaEntity disciplina = disciplinaOpt.orElse(null);
            
            // Calcular vagas disponíveis
            long vagasOcupadas = matriculaRepository.countBySalaIdAndStatus(salaId, "ATIVA");
            int vagasDisponiveis = sala.getLimiteVagas() - (int) vagasOcupadas;
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("salaId", sala.getId());
            result.put("identificador", sala.getIdentificador());
            result.put("horario", sala.getHorario());
            result.put("limiteVagas", sala.getLimiteVagas());
            result.put("vagasOcupadas", vagasOcupadas);
            result.put("vagasDisponiveis", vagasDisponiveis);
            
            if (disciplina != null) {
                result.put("disciplinaId", disciplina.getId());
                result.put("disciplinaNome", disciplina.getNome());
                result.put("disciplinaPeriodo", disciplina.getPeriodo());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar informações da sala: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/{alunoId}/salas-disponiveis - Listar todas as salas disponíveis para matrícula
     * Retorna salas com informações completas para exibir nos selects da tela de matrícula
     * Filtra por pré-requisitos: só mostra disciplinas que o aluno cumpriu os pré-requisitos
     */
    @GetMapping("/{alunoId}/salas-disponiveis")
    public ResponseEntity<?> listarSalasDisponiveis(@PathVariable Long alunoId) {
        try {
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aluno não encontrado"));
            }

            // Busca matrículas ativas do aluno para saber quais salas ele já está matriculado
            List<MatriculaEntity> matriculasAtivas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            Set<Long> salasMatriculadas = new HashSet<>();
            Set<Long> disciplinasMatriculadas = new HashSet<>();
            
            for (MatriculaEntity mat : matriculasAtivas) {
                salasMatriculadas.add(mat.getSalaId());
                salaRepository.findById(mat.getSalaId())
                    .ifPresent(sala -> disciplinasMatriculadas.add(sala.getDisciplinaId()));
            }
            
            // Busca disciplinas em que o aluno foi APROVADO para verificar pré-requisitos
            Set<Long> disciplinasAprovadas = new HashSet<>();
            List<MatriculaEntity> matriculasAprovadas = matriculaRepository.findByAlunoIdAndSituacao(alunoId, "APROVADO");
            for (MatriculaEntity mat : matriculasAprovadas) {
                salaRepository.findById(mat.getSalaId())
                    .ifPresent(sala -> disciplinasAprovadas.add(sala.getDisciplinaId()));
            }

            // Busca todas as salas
            List<SalaEntity> todasSalas = salaRepository.findAll();
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (SalaEntity sala : todasSalas) {
                // Ignora salas que o aluno já está matriculado
                if (salasMatriculadas.contains(sala.getId())) {
                    continue;
                }
                
                // Ignora disciplinas que o aluno já está matriculado em outra sala
                if (disciplinasMatriculadas.contains(sala.getDisciplinaId())) {
                    continue;
                }
                
                // Busca informações da disciplina
                Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
                if (disciplinaOpt.isEmpty()) continue;
                
                DisciplinaEntity disciplina = disciplinaOpt.get();
                
                // Verifica pré-requisitos
                Set<DisciplinaEntity> preRequisitos = disciplina.getPreRequisitos();
                if (preRequisitos != null && !preRequisitos.isEmpty()) {
                    boolean cumpreTodosPreRequisitos = true;
                    for (DisciplinaEntity preRequisito : preRequisitos) {
                        if (!disciplinasAprovadas.contains(preRequisito.getId())) {
                            cumpreTodosPreRequisitos = false;
                            break;
                        }
                    }
                    if (!cumpreTodosPreRequisitos) {
                        continue; // Não mostra disciplinas cujos pré-requisitos não foram cumpridos
                    }
                }
                
                // Conta vagas ocupadas
                long vagasOcupadas = matriculaRepository.countBySalaIdAndStatus(sala.getId(), "ATIVA");
                int vagasDisponiveis = sala.getLimiteVagas() - (int) vagasOcupadas;
                
                if (vagasDisponiveis <= 0) {
                    continue; // Não mostra salas sem vagas
                }
                
                // Busca nome do professor
                String nomeProfessor = usuarioRepository.findById(sala.getProfessorId())
                    .map(UsuarioEntity::getNome)
                    .orElse("Professor");
                
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", sala.getId());
                item.put("identificador", sala.getIdentificador());
                item.put("disciplinaId", disciplina.getId());
                item.put("disciplinaCodigo", disciplina.getCodigo());
                item.put("disciplinaNome", disciplina.getNome());
                item.put("disciplinaPeriodo", disciplina.getPeriodo());
                item.put("professorNome", nomeProfessor);
                item.put("horario", sala.getHorario());
                item.put("vagasDisponiveis", vagasDisponiveis);
                item.put("limiteVagas", sala.getLimiteVagas());
                
                resultado.add(item);
            }
            
            // Ordena por nome da disciplina
            resultado.sort((a, b) -> {
                String nomeA = (String) a.get("disciplinaNome");
                String nomeB = (String) b.get("disciplinaNome");
                return nomeA.compareToIgnoreCase(nomeB);
            });
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar salas: " + e.getMessage()));
        }
    }
}
