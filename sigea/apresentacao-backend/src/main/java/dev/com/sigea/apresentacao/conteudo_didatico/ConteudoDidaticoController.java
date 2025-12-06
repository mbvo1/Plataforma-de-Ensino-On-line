package dev.com.sigea.apresentacao.conteudo_didatico;

import dev.com.sigea.apresentacao.conteudo_didatico.dto.*;
import dev.com.sigea.apresentacao.conteudo_didatico.strategy.*;
import dev.com.sigea.apresentacao.conteudo_didatico.decorator.*;
import dev.com.sigea.dominio.turma.*;
import dev.com.sigea.dominio.usuario.UsuarioId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feature 01: Gerenciar Conteúdo Didático
 * Padrões: Strategy (validação de escopo e deadline) + Decorator (enriquecimento de materiais)
 */
@RestController
@RequestMapping("/api/professor/conteudo")
public class ConteudoDidaticoController {
    
    private final TurmaService turmaService;
    private final TurmaRepository turmaRepository;
    private final ValidacaoEscopoStrategy validacaoEscopoStrategy;
    
    public ConteudoDidaticoController(TurmaService turmaService, TurmaRepository turmaRepository) {
        this.turmaService = turmaService;
        this.turmaRepository = turmaRepository;
        this.validacaoEscopoStrategy = new ValidacaoTurmaProfessorStrategy(turmaRepository);
    }
    
    // ===== GESTÃO DE TURMAS =====
    
    @PostMapping("/turmas")
    public ResponseEntity<?> criarTurma(
            @RequestHeader("Professor-Id") String professorId,
            @RequestBody CriarTurmaRequest request) {
        try {
            Turma turma = new Turma(
                turmaRepository.proximoId(),
                new UsuarioId(professorId),
                request.getTitulo()
            );
            
            turmaRepository.salvar(turma);
            
            TurmaResponse response = new TurmaResponse();
            response.setId(turma.getId().toString());
            response.setTitulo(turma.getTitulo());
            response.setCodigoAcesso(turma.getCodigoAcesso().toString());
            response.setProfessorCriadorId(turma.getProfessorCriadorId().toString());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar turma: " + e.getMessage());
        }
    }
    
    @GetMapping("/turmas")
    public ResponseEntity<?> listarMinhasTurmas(
            @RequestHeader("Professor-Id") String professorId) {
        try {
            // Implementação simplificada - na prática, usar busca no repositório
            List<TurmaResponse> turmas = new ArrayList<>();
            return ResponseEntity.ok(turmas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao listar turmas: " + e.getMessage());
        }
    }
    
    // ===== GESTÃO DE MATERIAIS =====
    
    @PostMapping("/turmas/{turmaId}/materiais")
    public ResponseEntity<?> publicarMaterial(
            @RequestHeader("Professor-Id") String professorId,
            @PathVariable String turmaId,
            @RequestBody PublicarMaterialRequest request) {
        try {
            TurmaId turmaIdObj = new TurmaId(turmaId);
            UsuarioId professorIdObj = new UsuarioId(professorId);
            
            // Strategy Pattern: Validação de escopo
            validacaoEscopoStrategy.validar(professorIdObj, turmaIdObj);
            
            List<Anexo> anexos = request.getAnexos().stream()
                .map(a -> new Anexo(a.getNome()))
                .collect(Collectors.toList());
            
            Material material = turmaService.publicarMaterialNaTurma(
                professorIdObj,
                turmaIdObj,
                request.getTitulo(),
                request.getDescricao(),
                anexos
            );
            
            // Decorator Pattern: Enriquecimento do material
            MaterialEnriquecido materialEnriquecido = new MaterialBase(material);
            materialEnriquecido = new MaterialComVersao(materialEnriquecido, "1.0");
            materialEnriquecido = new MaterialComMetadata(materialEnriquecido, LocalDateTime.now());
            
            MaterialResponse response = new MaterialResponse();
            response.setId(material.getId().toString());
            response.setTitulo(materialEnriquecido.getTitulo());
            response.setDescricao(request.getDescricao());
            response.setVersao("1.0");
            response.setTemPrazo(false);
            
            List<AnexoResponse> anexosResponse = material.getAnexos().stream()
                .map(a -> {
                    AnexoResponse ar = new AnexoResponse();
                    ar.setNome(a.nomeArquivo());
                    ar.setUrl(""); // URL não está no domínio Anexo
                    return ar;
                })
                .collect(Collectors.toList());
            response.setAnexos(anexosResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao publicar material: " + e.getMessage());
        }
    }
    
    @GetMapping("/turmas/{turmaId}/materiais")
    public ResponseEntity<?> listarMateriais(@PathVariable String turmaId) {
        try {
            Turma turma = turmaRepository.buscarPorId(new TurmaId(turmaId))
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
            
            List<MaterialResponse> materiaisResponse = turma.getMateriais().stream()
                .map(material -> {
                    // Decorator Pattern: Enriquecimento
                    MaterialEnriquecido materialEnriquecido = new MaterialBase(material);
                    materialEnriquecido = new MaterialComVersao(materialEnriquecido, "1.0");
                    materialEnriquecido = new MaterialComMetadata(materialEnriquecido, LocalDateTime.now());
                    
                    MaterialResponse response = new MaterialResponse();
                    response.setId(material.getId().toString());
                    response.setTitulo(material.getTitulo());
                    response.setDescricao(materialEnriquecido.getDescricao());
                    response.setVersao("1.0");
                    response.setTemPrazo(false);
                    
                    List<AnexoResponse> anexos = material.getAnexos().stream()
                        .map(a -> {
                            AnexoResponse ar = new AnexoResponse();
                            ar.setNome(a.nomeArquivo());
                            ar.setUrl(""); // URL não está no domínio Anexo
                            return ar;
                        })
                        .collect(Collectors.toList());
                    response.setAnexos(anexos);
                    
                    return response;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(materiaisResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao listar materiais: " + e.getMessage());
        }
    }
    
    // ===== GESTÃO DE ATIVIDADES =====
    
    @PostMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<?> criarAtividade(
            @RequestHeader("Professor-Id") String professorId,
            @PathVariable String turmaId,
            @RequestBody CriarAtividadeRequest request) {
        try {
            TurmaId turmaIdObj = new TurmaId(turmaId);
            UsuarioId professorIdObj = new UsuarioId(professorId);
            
            // Strategy Pattern: Validação de escopo
            validacaoEscopoStrategy.validar(professorIdObj, turmaIdObj);
            
            // Strategy Pattern: Validação de deadline (prazo opcional)
            ValidacaoDeadlineStrategy deadlineStrategy = new DeadlineOpcionalStrategy();
            deadlineStrategy.validar(request.getDataLimite());
            
            Atividade atividade = turmaService.publicarAtividadeNaTurma(
                professorIdObj,
                turmaIdObj,
                request.getTitulo(),
                request.getDescricao(),
                request.getDataLimite()
            );
            
            AtividadeResponse response = new AtividadeResponse();
            response.setId(atividade.getId().toString());
            response.setTitulo(request.getTitulo());
            response.setDescricao(request.getDescricao());
            response.setDataLimite(request.getDataLimite());
            response.setTemPrazo(request.getDataLimite() != null);
            response.setTotalEnvios(0);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar atividade: " + e.getMessage());
        }
    }
    
    @GetMapping("/atividades/{atividadeId}/envios")
    public ResponseEntity<?> listarEnvios(@PathVariable String atividadeId) {
        try {
            // Implementação simplificada
            List<EnvioResponse> envios = new ArrayList<>();
            return ResponseEntity.ok(envios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao listar envios: " + e.getMessage());
        }
    }
    
    @PostMapping("/envios/{envioId}/corrigir")
    public ResponseEntity<?> corrigirAtividade(
            @PathVariable String envioId,
            @RequestBody CorrigirAtividadeRequest request) {
        try {
            // Validação: nota deve estar entre 0 e 10
            if (request.getNota() < 0 || request.getNota() > 10) {
                return ResponseEntity.badRequest()
                    .body("Valor inválido. A nota deve ser um número entre 0 e 10");
            }
            
            // Implementação da correção (simplificada)
            EnvioResponse response = new EnvioResponse();
            response.setId(envioId);
            response.setNota(request.getNota());
            response.setFeedback(request.getFeedback());
            response.setStatus("CORRIGIDO");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao corrigir atividade: " + e.getMessage());
        }
    }
}
