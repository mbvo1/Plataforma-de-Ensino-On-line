package dev.com.sigea.apresentacao.aluno;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller REST para turmas do aluno
 * 
 * Endpoints:
 * - GET /api/aluno/{alunoId}/turmas - Listar turmas do aluno
 * - GET /api/aluno/{alunoId}/turmas/{turmaId} - Detalhes de uma turma
 * - POST /api/aluno/{alunoId}/turmas/entrar - Entrar em turma com código
 * - GET /api/aluno/turmas/{turmaId}/avisos - Listar avisos da turma
 * - GET /api/aluno/turmas/{turmaId}/atividades - Listar atividades da turma
 * - GET /api/aluno/{alunoId}/atividades/{atividadeId}/envio - Verificar status de envio
 * - POST /api/aluno/{alunoId}/atividades/{atividadeId}/enviar - Enviar atividade
 */
@RestController
@RequestMapping("/api/aluno")
@CrossOrigin(origins = "*")
public class AlunoTurmasController {

    private final TurmaJpaRepository turmaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final TurmaAlunoJpaRepository turmaAlunoRepository;
    private final AvisoTurmaJpaRepository avisoTurmaRepository;
    private final AtividadeTurmaJpaRepository atividadeTurmaRepository;
    private final EnvioAtividadeJpaRepository envioAtividadeRepository;

    public AlunoTurmasController(TurmaJpaRepository turmaRepository, 
                                  UsuarioJpaRepository usuarioRepository,
                                  TurmaAlunoJpaRepository turmaAlunoRepository,
                                  AvisoTurmaJpaRepository avisoTurmaRepository,
                                  AtividadeTurmaJpaRepository atividadeTurmaRepository,
                                  EnvioAtividadeJpaRepository envioAtividadeRepository) {
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaAlunoRepository = turmaAlunoRepository;
        this.avisoTurmaRepository = avisoTurmaRepository;
        this.atividadeTurmaRepository = atividadeTurmaRepository;
        this.envioAtividadeRepository = envioAtividadeRepository;
    }

    /**
     * GET /api/aluno/{alunoId}/turmas - Listar turmas do aluno
     */
    @GetMapping("/{alunoId}/turmas")
    public ResponseEntity<?> listarTurmasDoAluno(@PathVariable Long alunoId) {
        // Verifica se o aluno existe
        Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Aluno não encontrado"));
        }

        // Busca as matrículas do aluno em turmas
        List<TurmaAlunoEntity> matriculas = turmaAlunoRepository.findByAlunoId(alunoId);
        
        // Mapeia para response
        List<Map<String, Object>> turmas = matriculas.stream()
            .map(matricula -> {
                TurmaEntity turma = matricula.getTurma();
                // Busca o nome do professor
                String nomeProfessor = "Professor";
                Optional<UsuarioEntity> professorOpt = usuarioRepository.findById(turma.getProfessorCriadorId());
                if (professorOpt.isPresent()) {
                    nomeProfessor = professorOpt.get().getNome();
                }
                
                Map<String, Object> turmaMap = new HashMap<>();
                turmaMap.put("turmaId", turma.getId());
                turmaMap.put("titulo", turma.getTitulo());
                turmaMap.put("nomeProfessor", nomeProfessor);
                turmaMap.put("codigoAcesso", turma.getCodigoAcesso());
                return turmaMap;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(turmas);
    }

    /**
     * POST /api/aluno/{alunoId}/turmas/entrar - Entrar em turma com código
     */
    @PostMapping("/{alunoId}/turmas/entrar")
    public ResponseEntity<?> entrarEmTurma(@PathVariable Long alunoId, 
                                            @RequestBody Map<String, String> request) {
        String codigoAcesso = request.get("codigoAcesso");
        
        if (codigoAcesso == null || codigoAcesso.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Código de acesso é obrigatório"));
        }

        // Verifica se o aluno existe
        Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Aluno não encontrado"));
        }

        // Busca a turma pelo código de acesso
        Optional<TurmaEntity> turmaOpt = turmaRepository.findByCodigoAcesso(codigoAcesso.toUpperCase().trim());
        if (turmaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Turma não encontrada. Verifique o código de acesso."));
        }
        TurmaEntity turma = turmaOpt.get();

        // Verifica se o aluno já está matriculado nessa turma
        boolean jaMatriculado = turmaAlunoRepository.existsByTurmaIdAndAlunoId(turma.getId(), alunoId);
        if (jaMatriculado) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Você já está matriculado nesta turma"));
        }

        // Cria a matrícula na turma
        TurmaAlunoEntity matricula = new TurmaAlunoEntity(turma.getId(), alunoId);
        turmaAlunoRepository.save(matricula);

        // Busca o nome do professor
        String nomeProfessor = "Professor";
        Optional<UsuarioEntity> professorOpt = usuarioRepository.findById(turma.getProfessorCriadorId());
        if (professorOpt.isPresent()) {
            nomeProfessor = professorOpt.get().getNome();
        }

        // Retorna dados da turma
        Map<String, Object> response = new HashMap<>();
        response.put("mensagem", "Você entrou na turma com sucesso!");
        response.put("turmaId", turma.getId());
        response.put("titulo", turma.getTitulo());
        response.put("nomeProfessor", nomeProfessor);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/aluno/{alunoId}/turmas/{turmaId} - Detalhes de uma turma específica
     */
    @GetMapping("/{alunoId}/turmas/{turmaId}")
    public ResponseEntity<?> obterDetalhesTurma(@PathVariable Long alunoId, @PathVariable Long turmaId) {
        // Verifica se o aluno está matriculado nesta turma
        boolean matriculado = turmaAlunoRepository.existsByTurmaIdAndAlunoId(turmaId, alunoId);
        if (!matriculado) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Você não está matriculado nesta turma"));
        }

        Optional<TurmaEntity> turmaOpt = turmaRepository.findById(turmaId);
        if (turmaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Turma não encontrada"));
        }

        TurmaEntity turma = turmaOpt.get();
        
        // Busca o nome do professor
        String nomeProfessor = "Professor";
        Optional<UsuarioEntity> professorOpt = usuarioRepository.findById(turma.getProfessorCriadorId());
        if (professorOpt.isPresent()) {
            nomeProfessor = professorOpt.get().getNome();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("turmaId", turma.getId());
        response.put("titulo", turma.getTitulo());
        response.put("nomeProfessor", nomeProfessor);
        // Não retorna o código de acesso para o aluno

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/aluno/turmas/{turmaId}/avisos - Listar avisos de uma turma
     */
    @GetMapping("/turmas/{turmaId}/avisos")
    public ResponseEntity<?> listarAvisosDaTurma(@PathVariable Long turmaId) {
        try {
            // Valida se a turma existe
            Optional<TurmaEntity> turmaOpt = turmaRepository.findById(turmaId);
            if (turmaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Turma não encontrada"));
            }

            // Busca todos os avisos da turma
            List<AvisoTurmaEntity> avisos = avisoTurmaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId);

            // Formata a resposta
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            List<Map<String, Object>> response = avisos.stream()
                .map(aviso -> {
                    UsuarioEntity professor = usuarioRepository.findById(aviso.getProfessorId()).orElse(null);
                    String nomeProfessor = professor != null ? professor.getNome() : "Professor";
                    String dataFormatada = aviso.getDataCriacao().format(formatter);

                    Map<String, Object> avisoMap = new HashMap<>();
                    avisoMap.put("avisoId", aviso.getId());
                    avisoMap.put("professorNome", nomeProfessor);
                    avisoMap.put("mensagem", aviso.getMensagem());
                    avisoMap.put("arquivoPath", aviso.getArquivoPath());
                    avisoMap.put("arquivoUrl", aviso.getArquivoConteudo());
                    avisoMap.put("dataPostagem", dataFormatada);
                    return avisoMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao buscar avisos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/turmas/{turmaId}/atividades - Listar atividades de uma turma
     */
    @GetMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<?> listarAtividadesDaTurma(@PathVariable Long turmaId) {
        try {
            // Valida se a turma existe
            Optional<TurmaEntity> turmaOpt = turmaRepository.findById(turmaId);
            if (turmaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Turma não encontrada"));
            }

            // Busca todas as atividades da turma
            List<AtividadeTurmaEntity> atividades = atividadeTurmaRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId);

            // Formata a resposta
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter prazoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            List<Map<String, Object>> response = atividades.stream()
                .map(atv -> {
                    UsuarioEntity professor = usuarioRepository.findById(atv.getProfessorId()).orElse(null);
                    String nomeProfessor = professor != null ? professor.getNome() : "Professor";
                    String dataFormatada = atv.getDataCriacao() != null ? atv.getDataCriacao().format(formatter) : "";
                    String prazoFormatado = atv.getPrazo() != null ? atv.getPrazo().format(prazoFormatter) : null;

                    Map<String, Object> atvMap = new HashMap<>();
                    atvMap.put("atividadeId", atv.getId());
                    atvMap.put("titulo", atv.getTitulo());
                    atvMap.put("descricao", atv.getDescricao());
                    atvMap.put("professorNome", nomeProfessor);
                    atvMap.put("arquivoPath", atv.getArquivoPath());
                    atvMap.put("arquivoUrl", atv.getArquivoConteudo());
                    atvMap.put("dataCriacao", dataFormatada);
                    atvMap.put("prazo", prazoFormatado);
                    return atvMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao buscar atividades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/aluno/{alunoId}/atividades/{atividadeId}/envio - Verificar status de envio
     */
    @GetMapping("/{alunoId}/atividades/{atividadeId}/envio")
    public ResponseEntity<?> verificarEnvio(@PathVariable Long alunoId, @PathVariable Long atividadeId) {
        try {
            Optional<EnvioAtividadeEntity> envioOpt = envioAtividadeRepository.findByAtividadeIdAndAlunoId(atividadeId, alunoId);
            
            if (envioOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "enviado", false,
                    "status", "NAO_ENVIADO"
                ));
            }
            
            EnvioAtividadeEntity envio = envioOpt.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            return ResponseEntity.ok(Map.of(
                "enviado", true,
                "envioId", envio.getId(),
                "status", envio.getStatus(),
                "arquivoPath", envio.getArquivoPath() != null ? envio.getArquivoPath() : "",
                "dataEnvio", envio.getDataEnvio().format(formatter),
                "nota", envio.getNota() != null ? envio.getNota() : "",
                "feedback", envio.getFeedbackProfessor() != null ? envio.getFeedbackProfessor() : ""
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao verificar envio: " + e.getMessage()));
        }
    }

    /**
     * POST /api/aluno/{alunoId}/atividades/{atividadeId}/enviar - Enviar atividade
     */
    @PostMapping("/{alunoId}/atividades/{atividadeId}/enviar")
    public ResponseEntity<?> enviarAtividade(
            @PathVariable Long alunoId, 
            @PathVariable Long atividadeId,
            @RequestParam("arquivo") MultipartFile arquivo) {
        try {
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Aluno não encontrado"));
            }

            // Verifica se a atividade existe
            Optional<AtividadeTurmaEntity> atividadeOpt = atividadeTurmaRepository.findById(atividadeId);
            if (atividadeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Atividade não encontrada"));
            }

            AtividadeTurmaEntity atividade = atividadeOpt.get();

            // Verifica se o prazo já passou - bloqueia envio
            if (atividade.getPrazo() != null && LocalDateTime.now().isAfter(atividade.getPrazo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "O prazo para envio desta atividade já expirou."));
            }

            // Verifica se já existe um envio
            Optional<EnvioAtividadeEntity> envioExistente = envioAtividadeRepository.findByAtividadeIdAndAlunoId(atividadeId, alunoId);
            
            // Converte o arquivo para base64 data URL
            String arquivoConteudo = null;
            if (arquivo != null && !arquivo.isEmpty()) {
                String mimeType = arquivo.getContentType();
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                byte[] bytes = arquivo.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                arquivoConteudo = "data:" + mimeType + ";base64," + base64;
            }
            
            EnvioAtividadeEntity envio;
            if (envioExistente.isPresent()) {
                // Atualiza o envio existente
                envio = envioExistente.get();
                envio.setArquivoPath(arquivo.getOriginalFilename());
                envio.setArquivoConteudo(arquivoConteudo);
                envio.setDataEnvio(LocalDateTime.now());
                envio.setStatus("ENVIADO");
            } else {
                // Cria novo envio
                envio = new EnvioAtividadeEntity(atividadeId, alunoId, arquivo.getOriginalFilename());
                envio.setArquivoConteudo(arquivoConteudo);
            }
            
            EnvioAtividadeEntity envioSalvo = envioAtividadeRepository.save(envio);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Atividade enviada com sucesso!",
                "envioId", envioSalvo.getId(),
                "status", envioSalvo.getStatus(),
                "arquivoPath", envioSalvo.getArquivoPath(),
                "dataEnvio", envioSalvo.getDataEnvio().format(formatter)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao enviar atividade: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/aluno/{alunoId}/atividades/{atividadeId}/envio - Cancelar envio
     */
    @DeleteMapping("/{alunoId}/atividades/{atividadeId}/envio")
    public ResponseEntity<?> cancelarEnvio(
            @PathVariable Long alunoId, 
            @PathVariable Long atividadeId) {
        try {
            // Verifica se a atividade existe
            Optional<AtividadeTurmaEntity> atividadeOpt = atividadeTurmaRepository.findById(atividadeId);
            if (atividadeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Atividade não encontrada"));
            }

            AtividadeTurmaEntity atividade = atividadeOpt.get();

            // Verifica se o prazo já passou - bloqueia cancelamento
            if (atividade.getPrazo() != null && LocalDateTime.now().isAfter(atividade.getPrazo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Não é possível cancelar o envio após o prazo."));
            }

            // Verifica se existe um envio
            Optional<EnvioAtividadeEntity> envioOpt = envioAtividadeRepository.findByAtividadeIdAndAlunoId(atividadeId, alunoId);
            
            if (envioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Nenhum envio encontrado para cancelar"));
            }

            EnvioAtividadeEntity envio = envioOpt.get();
            
            // Verifica se o envio já foi corrigido
            if ("CORRIGIDO".equals(envio.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Não é possível cancelar um envio já corrigido."));
            }

            envioAtividadeRepository.delete(envio);
            
            return ResponseEntity.ok(Map.of("mensagem", "Envio cancelado com sucesso!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao cancelar envio: " + e.getMessage()));
        }
    }
}
