package dev.com.sigea.apresentacao.atividades_aluno;

import dev.com.sigea.apresentacao.atividades_aluno.dto.EnviarAtividadeRequest;
import dev.com.sigea.apresentacao.atividades_aluno.dto.EnvioResponse;
import dev.com.sigea.apresentacao.atividades_aluno.dto.IngressarTurmaRequest;
import dev.com.sigea.apresentacao.atividades_aluno.strategy.*;
import dev.com.sigea.apresentacao.atividades_aluno.template.EnvioAtividadeTemplate;
import dev.com.sigea.apresentacao.atividades_aluno.template.EnvioNormalTemplate;
import dev.com.sigea.apresentacao.atividades_aluno.template.ReenvioTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller REST para atividades do aluno
 * 
 * Funcionalidade 05: Atividades (Aluno)
 * Padrões implementados:
 * - Strategy Pattern: Determina status do envio (PENDENTE, ENVIADO, ATRASADO, CORRIGIDO)
 * - Template Method Pattern: Fluxo de upload com bloqueio pós-correção
 */
@RestController
@RequestMapping("/api/aluno")
public class AtividadesAlunoController {
    
    private final Map<String, String> turmasAlunos = new HashMap<>();
    private final Map<String, Map<String, Object>> enviosArmazenados = new HashMap<>();
    
    /**
     * POST /api/aluno/turmas/ingressar - Ingressar em turma
     */
    @PostMapping("/turmas/ingressar")
    public ResponseEntity<Map<String, String>> ingressarTurma(@RequestBody IngressarTurmaRequest request) {
        turmasAlunos.put(request.getAlunoId(), request.getCodigoTurma());
        
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Ingresso realizado com sucesso na turma " + request.getCodigoTurma());
        response.put("alunoId", request.getAlunoId());
        response.put("codigoTurma", request.getCodigoTurma());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/aluno/turmas?alunoId=X - Listar turmas inscritas
     */
    @GetMapping("/turmas")
    public ResponseEntity<List<Map<String, String>>> listarTurmas(@RequestParam String alunoId) {
        List<Map<String, String>> turmas = new ArrayList<>();
        
        String codigoTurma = turmasAlunos.get(alunoId);
        if (codigoTurma != null) {
            Map<String, String> turma = new HashMap<>();
            turma.put("codigo", codigoTurma);
            turma.put("nome", "Turma " + codigoTurma);
            turmas.add(turma);
        }
        
        return ResponseEntity.ok(turmas);
    }
    
    /**
     * GET /api/aluno/turmas/{id}/materiais - Ver materiais da turma
     */
    @GetMapping("/turmas/{turmaId}/materiais")
    public ResponseEntity<List<Map<String, String>>> listarMateriais(@PathVariable String turmaId) {
        List<Map<String, String>> materiais = new ArrayList<>();
        
        Map<String, String> material = new HashMap<>();
        material.put("id", "MAT-001");
        material.put("titulo", "Introdução ao tema");
        material.put("tipo", "PDF");
        materiais.add(material);
        
        return ResponseEntity.ok(materiais);
    }
    
    /**
     * GET /api/aluno/turmas/{id}/atividades - Ver atividades da turma
     */
    @GetMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<List<Map<String, Object>>> listarAtividades(@PathVariable String turmaId) {
        List<Map<String, Object>> atividades = new ArrayList<>();
        
        Map<String, Object> atividade = new HashMap<>();
        atividade.put("id", "ATV-001");
        atividade.put("titulo", "Exercício 1");
        atividade.put("prazo", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        atividade.put("status", "PENDENTE");
        atividades.add(atividade);
        
        return ResponseEntity.ok(atividades);
    }
    
    /**
     * POST /api/aluno/atividades/enviar - Enviar atividade
     * Template Method Pattern: Fluxo validar → anexar → salvar → bloquear pós-correção
     */
    @PostMapping("/atividades/enviar")
    public ResponseEntity<Map<String, String>> enviarAtividade(@RequestBody EnviarAtividadeRequest request) {
        // Verifica se é reenvio
        boolean jaEnviou = enviosArmazenados.containsKey(request.getAtividadeId() + "-" + request.getAlunoId());
        boolean jaCorrigido = false; // Simulação - em produção verificaria no banco
        
        // Template Method Pattern: Escolhe template apropriado
        EnvioAtividadeTemplate template;
        if (jaEnviou) {
            template = new ReenvioTemplate(jaCorrigido);
        } else {
            template = new EnvioNormalTemplate();
        }
        
        // Executa fluxo de envio
        String envioId = template.enviarAtividade(
            request.getAtividadeId(),
            request.getAlunoId(),
            request.getArquivos()
        );
        
        // Armazena envio
        Map<String, Object> envio = new HashMap<>();
        envio.put("id", envioId);
        envio.put("atividadeId", request.getAtividadeId());
        envio.put("dataEnvio", LocalDateTime.now());
        envio.put("corrigido", false);
        enviosArmazenados.put(request.getAtividadeId() + "-" + request.getAlunoId(), envio);
        
        Map<String, String> response = new HashMap<>();
        response.put("envioId", envioId);
        response.put("mensagem", "Atividade enviada com sucesso");
        response.put("status", "ENVIADO");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/aluno/envios?alunoId=X - Ver status dos envios
     * Strategy Pattern: Determina status de cada envio
     */
    @GetMapping("/envios")
    public ResponseEntity<List<EnvioResponse>> listarEnvios(@RequestParam String alunoId) {
        List<EnvioResponse> envios = new ArrayList<>();
        
        // Strategy Pattern: Diferentes estratégias para diferentes status
        List<StatusEnvioStrategy> strategies = Arrays.asList(
            new StatusCorrigidoStrategy(),
            new StatusEnviadoStrategy(),
            new StatusPendenteStrategy()
        );
        
        enviosArmazenados.forEach((key, envio) -> {
            LocalDateTime dataEnvio = (LocalDateTime) envio.get("dataEnvio");
            LocalDateTime prazo = LocalDateTime.now().plusDays(7); // Simulação
            boolean corrigido = (boolean) envio.get("corrigido");
            
            // Tenta cada strategy até encontrar o status
            String status = null;
            for (StatusEnvioStrategy strategy : strategies) {
                status = strategy.determinarStatus(dataEnvio, prazo, corrigido);
                if (status != null) {
                    break;
                }
            }
            
            EnvioResponse response = new EnvioResponse();
            response.setId((String) envio.get("id"));
            response.setAtividadeId((String) envio.get("atividadeId"));
            response.setTitulo("Atividade");
            response.setStatus(status != null ? status : "PENDENTE");
            response.setDataEnvio(dataEnvio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.setPrazo(prazo.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            envios.add(response);
        });
        
        return ResponseEntity.ok(envios);
    }
}
