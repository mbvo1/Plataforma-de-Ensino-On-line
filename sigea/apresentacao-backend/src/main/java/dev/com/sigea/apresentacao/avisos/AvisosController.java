package dev.com.sigea.apresentacao.avisos;

import dev.com.sigea.apresentacao.avisos.decorator.*;
import dev.com.sigea.apresentacao.avisos.dto.AvisoResponse;
import dev.com.sigea.apresentacao.avisos.dto.MarcarLidoRequest;
import dev.com.sigea.apresentacao.avisos.dto.PublicarAvisoRequest;
import dev.com.sigea.apresentacao.avisos.observer.AvisoObserver;
import dev.com.sigea.apresentacao.avisos.observer.DashboardAlunoObserver;
import dev.com.sigea.apresentacao.avisos.observer.DashboardProfessorObserver;
import dev.com.sigea.apresentacao.avisos.observer.RegistroLeituraObserver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller REST para gerenciamento de avisos e comunicados
 * 
 * Funcionalidade 04: Avisos e Comunicados
 * Padrões implementados:
 * - Decorator Pattern: Enriquece avisos com prioridade, escopo, expiração, anexos
 * - Observer Pattern: Dissemina para dashboards e registra leituras
 */
@RestController
@RequestMapping("/api/avisos")
public class AvisosController {
    
    private final List<AvisoObserver> observers = new ArrayList<>();
    private final Map<String, AvisoEnriquecido> avisosArmazenados = new HashMap<>();
    
    public AvisosController() {
        // Registra observers
        observers.add(new DashboardAlunoObserver());
        observers.add(new DashboardProfessorObserver());
        observers.add(new RegistroLeituraObserver());
    }
    
    /**
     * POST /api/avisos - Publicar novo aviso
     * Decorator adiciona metadados, Observer dissemina
     */
    @PostMapping
    public ResponseEntity<AvisoResponse> publicarAviso(@RequestBody PublicarAvisoRequest request) {
        String avisoId = UUID.randomUUID().toString();
        
        // Decorator Pattern: Constrói aviso com decorações em camadas
        AvisoEnriquecido aviso = new AvisoBase(
            request.getTitulo(),
            request.getConteudo(),
            request.getAutorId(),
            request.getDisciplinaId()
        );
        
        // Adiciona prioridade
        if (request.getPrioridade() != null) {
            aviso = new AvisoComPrioridade(aviso, request.getPrioridade());
        }
        
        // Adiciona escopo
        if (request.getEscopo() != null) {
            aviso = new AvisoComEscopo(aviso, request.getEscopo());
        }
        
        // Adiciona expiração
        if (request.getDataExpiracao() != null) {
            LocalDateTime expiracao = LocalDateTime.parse(
                request.getDataExpiracao(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            aviso = new AvisoComExpiracao(aviso, expiracao);
        }
        
        avisosArmazenados.put(avisoId, aviso);
        
        // Observer Pattern: Notifica sobre novo aviso
        String escopo = request.getEscopo() != null ? request.getEscopo() : "GERAL";
        observers.forEach(obs -> obs.onAvisoPublicado(
            avisoId, 
            request.getTitulo(),
            escopo
        ));
        
        AvisoResponse response = new AvisoResponse();
        response.setId(avisoId);
        response.setTitulo(request.getTitulo());
        response.setConteudo(request.getConteudo());
        response.setAutorId(request.getAutorId());
        response.setDisciplinaId(request.getDisciplinaId());
        response.setPrioridade(request.getPrioridade());
        response.setEscopo(escopo);
        response.setDataExpiracao(request.getDataExpiracao());
        response.setLido(false);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/avisos?disciplinaId=X&usuarioId=Y - Listar avisos
     */
    @GetMapping
    public ResponseEntity<List<AvisoResponse>> listarAvisos(
            @RequestParam(required = false) String disciplinaId,
            @RequestParam(required = false) String usuarioId) {
        
        List<AvisoResponse> responses = new ArrayList<>();
        
        avisosArmazenados.forEach((id, aviso) -> {
            // Filtro simples - em produção seria mais sofisticado
            if (disciplinaId == null || aviso.getDisciplinaId().equals(disciplinaId)) {
                AvisoResponse resp = new AvisoResponse();
                resp.setId(id);
                resp.setTitulo(aviso.getTitulo());
                resp.setConteudo(aviso.getConteudo());
                resp.setAutorId(aviso.getAutorId());
                resp.setDisciplinaId(aviso.getDisciplinaId());
                responses.add(resp);
            }
        });
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * GET /api/avisos/{id} - Buscar aviso por ID
     */
    @GetMapping("/{avisoId}")
    public ResponseEntity<Map<String, Object>> buscarAviso(@PathVariable String avisoId) {
        AvisoEnriquecido aviso = avisosArmazenados.get(avisoId);
        
        if (aviso == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", avisoId);
        response.put("titulo", aviso.getTitulo());
        response.put("detalhesCompletos", aviso.getDetalhesCompletos());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/avisos/marcar-lido - Marcar aviso como lido
     * Observer registra leitura
     */
    @PostMapping("/marcar-lido")
    public ResponseEntity<Void> marcarComoLido(@RequestBody MarcarLidoRequest request) {
        // Observer Pattern: Notifica sobre leitura
        observers.forEach(obs -> obs.onAvisoLido(
            request.getUsuarioId(),
            request.getAvisoId()
        ));
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/avisos/nao-lidos?usuarioId=X - Listar avisos não lidos
     */
    @GetMapping("/nao-lidos")
    public ResponseEntity<List<AvisoResponse>> listarNaoLidos(
            @RequestParam String usuarioId) {
        
        // Implementação simplificada - em produção consultaria banco
        List<AvisoResponse> naoLidos = new ArrayList<>();
        
        avisosArmazenados.forEach((id, aviso) -> {
            AvisoResponse resp = new AvisoResponse();
            resp.setId(id);
            resp.setTitulo(aviso.getTitulo());
            resp.setLido(false);
            naoLidos.add(resp);
        });
        
        return ResponseEntity.ok(naoLidos);
    }
}
