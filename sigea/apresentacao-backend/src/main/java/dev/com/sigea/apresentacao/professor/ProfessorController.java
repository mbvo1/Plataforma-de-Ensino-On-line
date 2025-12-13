package dev.com.sigea.apresentacao.professor;

import dev.com.sigea.apresentacao.professor.dto.CriarTurmaRequest;
import dev.com.sigea.apresentacao.professor.dto.TurmaResponse;
import dev.com.sigea.infraestrutura.persistencia.AvisoTurmaEntity;
import dev.com.sigea.infraestrutura.persistencia.AvisoTurmaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.TurmaEntity;
import dev.com.sigea.infraestrutura.persistencia.TurmaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.AtividadeTurmaEntity;
import dev.com.sigea.infraestrutura.persistencia.AtividadeTurmaJpaRepository;
import dev.com.sigea.apresentacao.professor.CriarAtividadeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import dev.com.sigea.infraestrutura.persistencia.DisciplinaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.TopicoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.TopicoEntity;
import dev.com.sigea.infraestrutura.persistencia.TurmaAlunoEntity;
import dev.com.sigea.infraestrutura.persistencia.TurmaAlunoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.EnvioAtividadeEntity;
import dev.com.sigea.infraestrutura.persistencia.EnvioAtividadeJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/professor")
@CrossOrigin(origins = "*")
public class ProfessorController {
    
    private final TurmaJpaRepository turmaJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final AvisoTurmaJpaRepository avisoTurmaJpaRepository;
    private final AtividadeTurmaJpaRepository atividadeTurmaJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final SalaJpaRepository salaJpaRepository;
    private final TopicoJpaRepository topicoJpaRepository;
    private final TurmaAlunoJpaRepository turmaAlunoJpaRepository;
    private final EnvioAtividadeJpaRepository envioAtividadeJpaRepository;

    public ProfessorController(TurmaJpaRepository turmaJpaRepository, 
                               UsuarioJpaRepository usuarioJpaRepository,
                               AvisoTurmaJpaRepository avisoTurmaJpaRepository,
                               AtividadeTurmaJpaRepository atividadeTurmaJpaRepository,
                               DisciplinaJpaRepository disciplinaJpaRepository,
                               SalaJpaRepository salaJpaRepository,
                               TopicoJpaRepository topicoJpaRepository,
                               TurmaAlunoJpaRepository turmaAlunoJpaRepository,
                               EnvioAtividadeJpaRepository envioAtividadeJpaRepository) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.avisoTurmaJpaRepository = avisoTurmaJpaRepository;
        this.atividadeTurmaJpaRepository = atividadeTurmaJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.salaJpaRepository = salaJpaRepository;
        this.topicoJpaRepository = topicoJpaRepository;
        this.turmaAlunoJpaRepository = turmaAlunoJpaRepository;
        this.envioAtividadeJpaRepository = envioAtividadeJpaRepository;
    }

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * POST /api/professor/turmas/{turmaId}/atividades
     * Cria uma nova atividade para a turma
     */
    @PostMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<?> criarAtividade(@PathVariable Long turmaId, @RequestParam Long professorId, @RequestBody CriarAtividadeRequest request) {
        try {
            TurmaEntity turma = turmaJpaRepository.findById(turmaId).orElse(null);
            if (turma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Turma não encontrada"));
            }

            if (!turma.getProfessorCriadorId().equals(professorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Você não tem permissão para criar atividades nesta turma"));
            }

            // create without prazo first, then set prazo explicitly if provided
            AtividadeTurmaEntity atividade = new AtividadeTurmaEntity(
                turmaId,
                professorId,
                request.getTitulo(),
                request.getDescricao(),
                request.getArquivoPath(),
                null
            );
            if (request.getPrazo() != null) {
                atividade.setPrazo(request.getPrazo());
            } else {
                atividade.setPrazo(null);
            }
            // Salva o conteúdo do arquivo em base64
            atividade.setArquivoConteudo(request.getArquivoConteudo());

            AtividadeTurmaEntity salva = atividadeTurmaJpaRepository.save(atividade);

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("atividadeId", salva.getId());
            resp.put("titulo", salva.getTitulo());
            resp.put("descricao", salva.getDescricao());
            resp.put("arquivoPath", salva.getArquivoPath());
            // attempt to expose arquivoUrl when file saved under uploadDir
            if (salva.getArquivoPath() != null) {
                // If arquivoPath looks like a saved filename, expose /uploads/{filename}
                resp.put("arquivoUrl", "/uploads/" + salva.getArquivoPath());
            }
            resp.put("prazo", salva.getPrazo()); // may be null

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro ao criar atividade: " + e.getMessage()));
        }
    }

    /**
     * GET /api/professor/turmas/{turmaId}/atividades
     */
    @GetMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<?> listarAtividades(@PathVariable Long turmaId) {
        try {
            TurmaEntity turma = turmaJpaRepository.findById(turmaId).orElse(null);
            if (turma == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Turma não encontrada"));
            List<AtividadeTurmaEntity> atividades = atividadeTurmaJpaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            List<Map<String, Object>> response = new java.util.ArrayList<>();
            for (AtividadeTurmaEntity atv : atividades) {
                UsuarioEntity professor = usuarioJpaRepository.findById(atv.getProfessorId()).orElse(null);
                String nomeProfessor = professor != null ? professor.getNome() : "Professor";

                String prazoFormatado = null;
                if (atv.getPrazo() != null) prazoFormatado = atv.getPrazo().format(formatter);

                String dataCriacaoFormatada = atv.getDataCriacao() != null ? atv.getDataCriacao().format(formatter) : null;

                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("atividadeId", atv.getId());
                map.put("titulo", atv.getTitulo());
                map.put("descricao", atv.getDescricao());
                map.put("arquivoPath", atv.getArquivoPath());
                // expose arquivoUrl when the file exists in upload directory
                if (atv.getArquivoPath() != null) {
                    Path p = Path.of(uploadDir).resolve(atv.getArquivoPath());
                    if (Files.exists(p)) {
                        map.put("arquivoUrl", "/uploads/" + atv.getArquivoPath());
                    } else {
                        map.put("arquivoUrl", null);
                    }
                } else {
                    map.put("arquivoUrl", null);
                }
                map.put("prazo", prazoFormatado);
                map.put("prazoIso", atv.getPrazo() != null ? atv.getPrazo().toString() : null);
                map.put("dataCriacao", dataCriacaoFormatada);
                map.put("dataCriacaoIso", atv.getDataCriacao() != null ? atv.getDataCriacao().toString() : null);
                map.put("professorNome", nomeProfessor);

                response.add(map);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro ao buscar atividades: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/professor/turmas/{turmaId}/atividades/{atividadeId}
     * Atualiza uma atividade existente
     */
    @PutMapping("/turmas/{turmaId}/atividades/{atividadeId}")
    public ResponseEntity<?> atualizarAtividade(
            @PathVariable Long turmaId,
            @PathVariable Long atividadeId,
            @RequestParam Long professorId,
            @RequestBody CriarAtividadeRequest request) {
        try {
            AtividadeTurmaEntity atividade = atividadeTurmaJpaRepository.findById(atividadeId).orElse(null);
            if (atividade == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Atividade não encontrada"));
            if (!atividade.getTurmaId().equals(turmaId)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Atividade não pertence a esta turma"));
            if (!atividade.getProfessorId().equals(professorId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Você não tem permissão para editar"));

            // Atualiza campos
            atividade.setTitulo(request.getTitulo());
            atividade.setDescricao(request.getDescricao());
            atividade.setArquivoPath(request.getArquivoPath());
            atividade.setPrazo(request.getPrazo());

            AtividadeTurmaEntity salva = atividadeTurmaJpaRepository.save(atividade);

            UsuarioEntity professor = usuarioJpaRepository.findById(professorId).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String prazoFormatado = salva.getPrazo() != null ? salva.getPrazo().format(formatter) : null;
            String dataCriacaoFormatada = salva.getDataCriacao() != null ? salva.getDataCriacao().format(formatter) : null;

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("atividadeId", salva.getId());
            resp.put("titulo", salva.getTitulo());
            resp.put("descricao", salva.getDescricao());
            resp.put("arquivoPath", salva.getArquivoPath());
            if (salva.getArquivoPath() != null) resp.put("arquivoUrl", "/uploads/" + salva.getArquivoPath());
            resp.put("prazo", prazoFormatado);
            resp.put("prazoIso", salva.getPrazo() != null ? salva.getPrazo().toString() : null);
            resp.put("dataCriacao", dataCriacaoFormatada);
            resp.put("dataCriacaoIso", salva.getDataCriacao() != null ? salva.getDataCriacao().toString() : null);
            resp.put("professorNome", nomeProfessor);

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Erro ao atualizar atividade: " + e.getMessage()));
        }
    }

    /**
     * POST /api/professor/turmas/{turmaId}/atividades/upload
     * Create activity with file upload (multipart/form-data)
     */
    @PostMapping(value = "/turmas/{turmaId}/atividades/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> criarAtividadeComUpload(
            @PathVariable Long turmaId,
            @RequestParam Long professorId,
            @RequestParam String titulo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String prazo,
            @RequestParam(required = false) MultipartFile arquivo
    ) {
        try {
            TurmaEntity turma = turmaJpaRepository.findById(turmaId).orElse(null);
            if (turma == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Turma não encontrada"));
            if (!turma.getProfessorCriadorId().equals(professorId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Você não tem permissão para criar atividades nesta turma"));

            String storedFilename = null;
            String originalFilename = null;
            if (arquivo != null && !arquivo.isEmpty()) {
                originalFilename = arquivo.getOriginalFilename();
                String safe = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]","_");
                Path uploadPath = Path.of(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Path target = uploadPath.resolve(safe);
                try {
                    Files.copy(arquivo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                    storedFilename = safe;
                } catch (IOException ex) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Erro ao salvar arquivo: " + ex.getMessage()));
                }
            }

            AtividadeTurmaEntity atividade = new AtividadeTurmaEntity(
                    turmaId,
                    professorId,
                    titulo,
                    descricao,
                    storedFilename, // store saved filename in DB
                    null
            );
            // parse prazo if provided (ISO or datetime-local)
            if (prazo != null && !prazo.isBlank()) {
                try {
                    atividade.setPrazo(java.time.LocalDateTime.parse(prazo));
                } catch (Exception ex) {
                    // ignore parse error, keep null
                }
            }

            AtividadeTurmaEntity salva = atividadeTurmaJpaRepository.save(atividade);

            java.util.Map<String,Object> resp = new java.util.HashMap<>();
            resp.put("atividadeId", salva.getId());
            resp.put("titulo", salva.getTitulo());
            resp.put("descricao", salva.getDescricao());
            resp.put("arquivoPath", originalFilename != null ? originalFilename : null); // display original name
            resp.put("arquivoUrl", storedFilename != null ? "/uploads/" + storedFilename : null);
            resp.put("prazo", salva.getPrazo());

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Erro ao criar atividade: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/professor/turmas/{turmaId}/atividades/{atividadeId}
     */
    @DeleteMapping("/turmas/{turmaId}/atividades/{atividadeId}")
    public ResponseEntity<?> removerAtividade(@PathVariable Long turmaId, @PathVariable Long atividadeId, @RequestParam Long professorId) {
        try {
            AtividadeTurmaEntity atividade = atividadeTurmaJpaRepository.findById(atividadeId).orElse(null);
            if (atividade == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Atividade não encontrada"));
            if (!atividade.getTurmaId().equals(turmaId)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Atividade não pertence a esta turma"));
            if (!atividade.getProfessorId().equals(professorId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Você não tem permissão para excluir"));

            atividadeTurmaJpaRepository.delete(atividade);
            return ResponseEntity.ok(Map.of("message","Atividade excluída"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Erro ao excluir atividade: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/professor/turmas?professorId={id}
     * Lista todas as turmas de um professor
     */
    @GetMapping("/turmas")
    public ResponseEntity<?> listarTurmasDoProfessor(@RequestParam Long professorId) {
        try {
            List<TurmaEntity> turmas = turmaJpaRepository.findByProfessorCriadorId(professorId);
            
            // Busca o nome do professor
            UsuarioEntity professor = usuarioJpaRepository.findById(professorId).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";
            
            List<TurmaResponse> response = turmas.stream()
                .map(turma -> new TurmaResponse(
                    turma.getId(),
                    turma.getTitulo(),
                    nomeProfessor
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar turmas: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/professor/turmas
     * Cria uma nova turma
     */
    @PostMapping("/turmas")
    public ResponseEntity<?> criarTurma(@RequestBody CriarTurmaRequest request, @RequestParam Long professorId) {
        try {
            // Validações básicas
            if (request.getNomeTurma() == null || request.getNomeTurma().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nome da turma é obrigatório"));
            }
            
            if (request.getCodigoAcesso() == null || request.getCodigoAcesso().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Código de acesso é obrigatório"));
            }
            
            // Cria a nova turma
            TurmaEntity novaTurma = new TurmaEntity();
            novaTurma.setTitulo(request.getNomeTurma());
            novaTurma.setProfessorCriadorId(professorId);
            novaTurma.setCodigoAcesso(request.getCodigoAcesso());
            
            TurmaEntity turmaSalva = turmaJpaRepository.save(novaTurma);
            
            // Busca o nome do professor
            UsuarioEntity professor = usuarioJpaRepository.findById(professorId).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";
            
            TurmaResponse response = new TurmaResponse(
                turmaSalva.getId(),
                turmaSalva.getTitulo(),
                nomeProfessor
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao criar turma: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/professor/turmas/{id}
     * Busca uma turma específica por ID
     */
    @GetMapping("/turmas/{id}")
    public ResponseEntity<?> buscarTurmaPorId(@PathVariable Long id) {
        try {
            TurmaEntity turma = turmaJpaRepository.findById(id).orElse(null);
            
            if (turma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Turma não encontrada"));
            }
            
            // Busca o nome do professor
            UsuarioEntity professor = usuarioJpaRepository.findById(turma.getProfessorCriadorId()).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";
            
            // Retorna com código de acesso também
            Map<String, Object> response = Map.of(
                "turmaId", turma.getId(),
                "titulo", turma.getTitulo(),
                "nomeProfessor", nomeProfessor,
                "codigoAcesso", turma.getCodigoAcesso()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar turma: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/professor/turmas/{id}/avisos
     * Cria um novo aviso na turma
     */
    @PostMapping("/turmas/{turmaId}/avisos")
    public ResponseEntity<?> criarAviso(@PathVariable Long turmaId, 
                                       @RequestBody CriarAvisoRequest request,
                                       @RequestParam Long professorId) {
        try {
            // Valida se a turma existe
            TurmaEntity turma = turmaJpaRepository.findById(turmaId).orElse(null);
            if (turma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Turma não encontrada"));
            }
            
            // Valida se o professor é o criador da turma
            if (!turma.getProfessorCriadorId().equals(professorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Você não tem permissão para postar avisos nesta turma"));
            }
            
            // Cria o aviso
            AvisoTurmaEntity aviso = new AvisoTurmaEntity(
                turmaId,
                professorId,
                request.getMensagem(),
                request.getArquivoPath()
            );
            aviso.setArquivoConteudo(request.getArquivoConteudo());
            
            AvisoTurmaEntity avisoSalvo = avisoTurmaJpaRepository.save(aviso);
            
            // Busca o nome do professor
            UsuarioEntity professor = usuarioJpaRepository.findById(professorId).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";
            
            // Formata a data com hora
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dataFormatada = avisoSalvo.getDataCriacao().format(formatter);
            
            AvisoTurmaResponse response = new AvisoTurmaResponse(
                avisoSalvo.getId(),
                nomeProfessor,
                avisoSalvo.getMensagem(),
                avisoSalvo.getArquivoPath(),
                avisoSalvo.getArquivoConteudo(),
                dataFormatada
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao criar aviso: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/professor/turmas/{id}/avisos
     * Lista todos os avisos de uma turma
     */
    @GetMapping("/turmas/{turmaId}/avisos")
    public ResponseEntity<?> listarAvisos(@PathVariable Long turmaId) {
        try {
            // Valida se a turma existe
            TurmaEntity turma = turmaJpaRepository.findById(turmaId).orElse(null);
            if (turma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Turma não encontrada"));
            }
            
            // Busca todos os avisos da turma
            List<AvisoTurmaEntity> avisos = avisoTurmaJpaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId);
            
            // Formata a resposta
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            List<AvisoTurmaResponse> response = avisos.stream()
                .map(aviso -> {
                    UsuarioEntity professor = usuarioJpaRepository.findById(aviso.getProfessorId()).orElse(null);
                    String nomeProfessor = professor != null ? professor.getNome() : "Professor";
                    String dataFormatada = aviso.getDataCriacao().format(formatter);
                    
                    // Retorna arquivoConteudo como arquivoUrl se disponível
                    String arquivoUrl = aviso.getArquivoConteudo();
                    
                    return new AvisoTurmaResponse(
                        aviso.getId(),
                        nomeProfessor,
                        aviso.getMensagem(),
                        aviso.getArquivoPath(),
                        arquivoUrl,
                        dataFormatada
                    );
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar avisos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/professor/disciplinas?professorId={id}
     * Lista as disciplinas para as quais o professor é responsável (baseado nas salas)
     */
    @GetMapping("/disciplinas")
    public ResponseEntity<?> listarDisciplinasDoProfessor(@RequestParam Long professorId) {
        try {
                List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> todasSalas = salaJpaRepository.findAll();
                List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> salas = todasSalas.stream()
                    .filter(s -> s.getProfessorId() != null && s.getProfessorId().equals(professorId))
                    .collect(java.util.stream.Collectors.toList());
                if (salas == null || salas.isEmpty()) return ResponseEntity.ok(List.of());

                // Return one entry per sala that the professor is responsible for,
                // including the disciplina name and the sala identificador so the
                // frontend can render "<nome da disciplina>: <identificador da sala>".
                List<java.util.Map<String,Object>> result = new java.util.ArrayList<>();
                for (dev.com.sigea.infraestrutura.persistencia.SalaEntity s : salas) {
                    Long did = s.getDisciplinaId();
                    var discOpt = disciplinaJpaRepository.findById(did);
                    String nome = discOpt.map(d -> d.getNome()).orElse("Disciplina " + did);
                    String codigo = discOpt.map(d -> d.getCodigo()).orElse(null);

                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("disciplinaId", did);
                    m.put("nome", nome);
                    m.put("codigo", codigo);
                    m.put("salaId", s.getId());
                    m.put("identificador", s.getIdentificador());
                    // alunos matriculados not implemented; default to 0
                    m.put("alunosMatriculados", 0L);

                    result.add(m);
                }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Erro ao buscar disciplinas: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/professor/turmas/{turmaId}/avisos/{avisoId}
     * Atualiza um aviso existente
     */
    @PutMapping("/turmas/{turmaId}/avisos/{avisoId}")
    public ResponseEntity<?> atualizarAviso(
            @PathVariable Long turmaId,
            @PathVariable Long avisoId,
            @RequestParam Long professorId,
            @RequestBody CriarAvisoRequest request) {
        try {
            // Busca o aviso
            AvisoTurmaEntity aviso = avisoTurmaJpaRepository.findById(avisoId).orElse(null);
            if (aviso == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aviso não encontrado"));
            }
            
            // Valida se o aviso pertence à turma
            if (!aviso.getTurmaId().equals(turmaId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Aviso não pertence a esta turma"));
            }
            
            // Valida se o professor é o criador do aviso
            if (!aviso.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Você não tem permissão para editar este aviso"));
            }
            
            // Atualiza os campos
            aviso.setMensagem(request.getMensagem());
            aviso.setArquivoPath(request.getArquivoPath());
            
            // Salva as alterações
            AvisoTurmaEntity avisoAtualizado = avisoTurmaJpaRepository.save(aviso);
            
            // Busca o nome do professor
            UsuarioEntity professor = usuarioJpaRepository.findById(professorId).orElse(null);
            String nomeProfessor = professor != null ? professor.getNome() : "Professor";
            
            // Formata a data com hora
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dataFormatada = avisoAtualizado.getDataCriacao().format(formatter);
            
            AvisoTurmaResponse response = new AvisoTurmaResponse(
                avisoAtualizado.getId(),
                nomeProfessor,
                avisoAtualizado.getMensagem(),
                avisoAtualizado.getArquivoPath(),
                avisoAtualizado.getArquivoConteudo(),
                dataFormatada
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao atualizar aviso: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/professor/turmas/{turmaId}/avisos/{avisoId}
     * Remove um aviso
     */
    @DeleteMapping("/turmas/{turmaId}/avisos/{avisoId}")
    public ResponseEntity<?> removerAviso(
            @PathVariable Long turmaId,
            @PathVariable Long avisoId,
            @RequestParam Long professorId) {
        try {
            AvisoTurmaEntity aviso = avisoTurmaJpaRepository.findById(avisoId).orElse(null);
            if (aviso == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aviso não encontrado"));
            }

            if (!aviso.getTurmaId().equals(turmaId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Aviso não pertence a esta turma"));
            }

            if (!aviso.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Você não tem permissão para excluir este aviso"));
            }

            avisoTurmaJpaRepository.delete(aviso);

            return ResponseEntity.ok(Map.of("message", "Aviso excluído com sucesso"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao excluir aviso: " + e.getMessage()));
        }
    }

    /**
     * GET /api/professor/salas?professorId={id}
     * Lista as salas (disciplinas) que o professor é responsável
     */
    @GetMapping("/salas")
    public ResponseEntity<?> listarSalasDoProfessor(@RequestParam Long professorId) {
        try {
            List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> salas = 
                salaJpaRepository.findByProfessorId(professorId);
            
            List<Map<String, Object>> response = salas.stream()
                .filter(sala -> "ATIVO".equalsIgnoreCase(sala.getStatus()))
                .map(sala -> {
                    Map<String, Object> salaMap = new java.util.HashMap<>();
                    salaMap.put("id", sala.getId());
                    salaMap.put("identificador", sala.getIdentificador());
                    salaMap.put("disciplinaId", sala.getDisciplinaId());
                    salaMap.put("professorId", sala.getProfessorId());
                    salaMap.put("horario", sala.getHorario());
                    salaMap.put("limiteVagas", sala.getLimiteVagas());
                    salaMap.put("status", sala.getStatus());
                    
                    // Buscar nome da disciplina
                    disciplinaJpaRepository.findById(sala.getDisciplinaId())
                        .ifPresent(disciplina -> {
                            salaMap.put("disciplinaNome", disciplina.getNome());
                            salaMap.put("disciplinaCodigo", disciplina.getCodigo());
                        });
                    
                    return salaMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar salas: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/professor/aulas-hoje?professorId={id}&diaSemana={dia}
     * Retorna as aulas do dia baseado no horário das salas
     */
    @GetMapping("/aulas-hoje")
    public ResponseEntity<?> listarAulasHoje(
            @RequestParam Long professorId,
            @RequestParam String diaSemana) {
        try {
            // Busca todas as salas do professor
            List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> salas = salaJpaRepository.findAll().stream()
                .filter(s -> s.getProfessorId() != null && s.getProfessorId().equals(professorId))
                .collect(Collectors.toList());
            
            List<java.util.Map<String, Object>> aulas = new java.util.ArrayList<>();
            
            for (var sala : salas) {
                String horario = sala.getHorario();
                
                // Verifica se o horário contém o dia da semana atual
                if (horario != null && horario.toLowerCase().contains(diaSemana.toLowerCase())) {
                    var discOpt = disciplinaJpaRepository.findById(sala.getDisciplinaId());
                    String disciplinaNome = discOpt.map(d -> d.getNome()).orElse("Disciplina " + sala.getDisciplinaId());
                    
                    java.util.Map<String, Object> aula = new java.util.HashMap<>();
                    aula.put("salaId", sala.getId());
                    aula.put("disciplinaNome", disciplinaNome);
                    aula.put("salaIdentificador", sala.getIdentificador());
                    aula.put("horario", horario);
                    aulas.add(aula);
                }
            }
            
            return ResponseEntity.ok(aulas);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar aulas: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/professor/forum/mensagens-novas?professorId={id}
     * Retorna tópicos novos (não lidos) das disciplinas do professor
     */
    @GetMapping("/forum/mensagens-novas")
    public ResponseEntity<?> listarMensagensNovas(@RequestParam Long professorId) {
        try {
            // Busca todas as salas (disciplinas) do professor
            List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> salas = salaJpaRepository.findAll().stream()
                .filter(s -> s.getProfessorId() != null && s.getProfessorId().equals(professorId))
                .collect(Collectors.toList());
            
            // Coleta os IDs das disciplinas
            java.util.Set<Long> disciplinaIds = salas.stream()
                .map(s -> s.getDisciplinaId())
                .collect(java.util.stream.Collectors.toSet());
            
            List<java.util.Map<String, Object>> mensagens = new java.util.ArrayList<>();
            
            // Para cada disciplina, busca os tópicos recentes (criados por outros usuários)
            for (Long disciplinaId : disciplinaIds) {
                var discOpt = disciplinaJpaRepository.findById(disciplinaId);
                String disciplinaNome = discOpt.map(d -> d.getNome()).orElse("Disciplina " + disciplinaId);
                
                // Busca tópicos da disciplina que não foram criados pelo professor
                List<TopicoEntity> topicos = topicoJpaRepository.findByDisciplinaIdOrderByIdDesc(disciplinaId);
                
                for (TopicoEntity topico : topicos) {
                    // Filtra tópicos que não foram criados pelo professor
                    if (!topico.getAutorId().equals(professorId)) {
                        // Busca nome do autor
                        String autorNome = usuarioJpaRepository.findById(topico.getAutorId())
                            .map(u -> u.getNome())
                            .orElse("Usuário " + topico.getAutorId());
                        
                        java.util.Map<String, Object> msg = new java.util.HashMap<>();
                        msg.put("topicoId", topico.getId());
                        msg.put("disciplinaId", disciplinaId);
                        msg.put("disciplinaNome", disciplinaNome);
                        msg.put("titulo", topico.getTitulo());
                        msg.put("autorNome", autorNome);
                        mensagens.add(msg);
                        
                        // Limita a 5 mensagens no dashboard
                        if (mensagens.size() >= 5) break;
                    }
                }
                
                if (mensagens.size() >= 5) break;
            }
            
            return ResponseEntity.ok(mensagens);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar mensagens: " + e.getMessage()));
        }
    }

    /**
     * GET /api/professor/atividades/{atividadeId}/envios
     * Lista todos os alunos da turma com status de envio da atividade
     */
    @GetMapping("/atividades/{atividadeId}/envios")
    public ResponseEntity<?> listarEnviosAtividade(@PathVariable Long atividadeId) {
        try {
            // Busca a atividade
            Optional<AtividadeTurmaEntity> atividadeOpt = atividadeTurmaJpaRepository.findById(atividadeId);
            if (atividadeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Atividade não encontrada"));
            }
            
            AtividadeTurmaEntity atividade = atividadeOpt.get();
            Long turmaId = atividade.getTurmaId();
            
            // Busca todos os alunos da turma
            List<TurmaAlunoEntity> alunosTurma = turmaAlunoJpaRepository.findByTurmaId(turmaId);
            
            // Busca todos os envios da atividade
            List<EnvioAtividadeEntity> envios = envioAtividadeJpaRepository.findByAtividadeId(atividadeId);
            
            // Mapa de alunoId -> envio para facilitar lookup
            Map<Long, EnvioAtividadeEntity> enviosPorAluno = envios.stream()
                .collect(Collectors.toMap(EnvioAtividadeEntity::getAlunoId, e -> e, (e1, e2) -> e1));
            
            // Monta a lista de respostas
            List<Map<String, Object>> resultado = new ArrayList<>();
            int entregues = 0;
            int pendentes = 0;
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (TurmaAlunoEntity alunoTurma : alunosTurma) {
                Long alunoId = alunoTurma.getAlunoId();
                
                // Busca nome do aluno
                String nomeAluno = usuarioJpaRepository.findById(alunoId)
                    .map(UsuarioEntity::getNome)
                    .orElse("Aluno " + alunoId);
                
                EnvioAtividadeEntity envio = enviosPorAluno.get(alunoId);
                
                Map<String, Object> alunoInfo = new java.util.HashMap<>();
                alunoInfo.put("alunoId", alunoId);
                alunoInfo.put("nomeAluno", nomeAluno);
                
                if (envio != null) {
                    alunoInfo.put("status", "Entregue");
                    alunoInfo.put("arquivoPath", envio.getArquivoPath());
                    alunoInfo.put("arquivoConteudo", envio.getArquivoConteudo());
                    alunoInfo.put("dataEnvio", envio.getDataEnvio() != null ? envio.getDataEnvio().format(formatter) : "");
                    alunoInfo.put("nota", envio.getNota());
                    alunoInfo.put("envioId", envio.getId());
                    alunoInfo.put("statusEnvio", envio.getStatus());
                    entregues++;
                } else {
                    alunoInfo.put("status", "Pendente");
                    alunoInfo.put("arquivoPath", null);
                    alunoInfo.put("arquivoConteudo", null);
                    alunoInfo.put("dataEnvio", null);
                    alunoInfo.put("nota", null);
                    alunoInfo.put("envioId", null);
                    alunoInfo.put("statusEnvio", null);
                    pendentes++;
                }
                
                resultado.add(alunoInfo);
            }
            
            // Ordena por nome do aluno
            resultado.sort((a, b) -> {
                String nomeA = (String) a.get("nomeAluno");
                String nomeB = (String) b.get("nomeAluno");
                return nomeA.compareToIgnoreCase(nomeB);
            });
            
            // Monta resposta final
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("atividadeId", atividadeId);
            response.put("turmaId", turmaId);
            response.put("entregues", entregues);
            response.put("pendentes", pendentes);
            response.put("alunos", resultado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao buscar envios: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/professor/atividades/{atividadeId}/envios/{envioId}/nota
     * Atribui nota a um envio de atividade
     */
    @PutMapping("/atividades/{atividadeId}/envios/{envioId}/nota")
    public ResponseEntity<?> atribuirNota(
            @PathVariable Long atividadeId,
            @PathVariable Long envioId,
            @RequestBody Map<String, Object> request) {
        try {
            // Busca o envio
            Optional<EnvioAtividadeEntity> envioOpt = envioAtividadeJpaRepository.findById(envioId);
            if (envioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Envio não encontrado"));
            }
            
            EnvioAtividadeEntity envio = envioOpt.get();
            
            // Verifica se o envio é da atividade correta
            if (!envio.getAtividadeId().equals(atividadeId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Envio não pertence a esta atividade"));
            }
            
            // Atualiza a nota
            Object notaObj = request.get("nota");
            Double nota = null;
            if (notaObj != null) {
                if (notaObj instanceof Number) {
                    nota = ((Number) notaObj).doubleValue();
                } else if (notaObj instanceof String && !((String) notaObj).isEmpty()) {
                    nota = Double.parseDouble((String) notaObj);
                }
            }
            
            envio.setNota(nota);
            envio.setStatus("CORRIGIDO");
            
            // Atualiza feedback se fornecido
            Object feedbackObj = request.get("feedback");
            if (feedbackObj != null && feedbackObj instanceof String) {
                envio.setFeedbackProfessor((String) feedbackObj);
            }
            
            envioAtividadeJpaRepository.save(envio);
            
            return ResponseEntity.ok(Map.of(
                "message", "Nota atribuída com sucesso",
                "envioId", envioId,
                "nota", nota != null ? nota : "",
                "status", envio.getStatus()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao atribuir nota: " + e.getMessage()));
        }
    }

    /**
     * POST /api/professor/atividades/{atividadeId}/envios/salvar-notas
     * Salva múltiplas notas de uma vez
     */
    @PostMapping("/atividades/{atividadeId}/envios/salvar-notas")
    public ResponseEntity<?> salvarNotas(
            @PathVariable Long atividadeId,
            @RequestBody List<Map<String, Object>> notas) {
        try {
            int atualizados = 0;
            
            for (Map<String, Object> item : notas) {
                Object envioIdObj = item.get("envioId");
                Object notaObj = item.get("nota");
                
                if (envioIdObj == null) continue;
                
                Long envioId = ((Number) envioIdObj).longValue();
                
                Optional<EnvioAtividadeEntity> envioOpt = envioAtividadeJpaRepository.findById(envioId);
                if (envioOpt.isEmpty()) continue;
                
                EnvioAtividadeEntity envio = envioOpt.get();
                
                // Verifica se o envio é da atividade correta
                if (!envio.getAtividadeId().equals(atividadeId)) continue;
                
                // Atualiza a nota
                Double nota = null;
                if (notaObj != null) {
                    if (notaObj instanceof Number) {
                        nota = ((Number) notaObj).doubleValue();
                    } else if (notaObj instanceof String && !((String) notaObj).isEmpty()) {
                        try {
                            nota = Double.parseDouble((String) notaObj);
                        } catch (NumberFormatException e) {
                            // ignora nota inválida
                        }
                    }
                }
                
                if (nota != null) {
                    envio.setNota(nota);
                    envio.setStatus("CORRIGIDO");
                    envioAtividadeJpaRepository.save(envio);
                    atualizados++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Notas salvas com sucesso",
                "atualizados", atualizados
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao salvar notas: " + e.getMessage()));
        }
    }
}
