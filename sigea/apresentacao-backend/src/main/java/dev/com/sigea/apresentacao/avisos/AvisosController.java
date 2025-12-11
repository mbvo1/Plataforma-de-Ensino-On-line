package dev.com.sigea.apresentacao.avisos;

import dev.com.sigea.apresentacao.avisos.decorator.*;
import dev.com.sigea.apresentacao.avisos.dto.AvisoResponse;
import dev.com.sigea.apresentacao.avisos.dto.MarcarLidoRequest;
import dev.com.sigea.apresentacao.avisos.dto.PublicarAvisoRequest;
import dev.com.sigea.apresentacao.avisos.observer.AvisoObserver;
import dev.com.sigea.apresentacao.avisos.observer.DashboardAlunoObserver;
import dev.com.sigea.apresentacao.avisos.observer.DashboardProfessorObserver;
import dev.com.sigea.apresentacao.avisos.observer.RegistroLeituraObserver;
import dev.com.sigea.infraestrutura.persistencia.AvisoEntity;
import dev.com.sigea.infraestrutura.persistencia.AvisoJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.AvisoLeituraEntity;
import dev.com.sigea.infraestrutura.persistencia.AvisoLeituraJpaRepository;
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
    private final Map<String, String> escoposArmazenados = new HashMap<>();
    private final AvisoJpaRepository avisoJpaRepository;
    private final AvisoLeituraJpaRepository leituraJpaRepository;
    
    public AvisosController(AvisoJpaRepository avisoJpaRepository, 
                           AvisoLeituraJpaRepository leituraJpaRepository) {
        this.avisoJpaRepository = avisoJpaRepository;
        this.leituraJpaRepository = leituraJpaRepository;
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
        
        // Armazena escopo separadamente para fácil acesso
        String escopo = request.getEscopo() != null ? request.getEscopo() : "GERAL";
        escoposArmazenados.put(avisoId, escopo);
        
        // Mapeia escopo para alvo_tipo do banco
        String alvoTipo;
        switch(escopo) {
            case "GERAL" -> alvoTipo = "GERAL";
            case "ALUNOS" -> alvoTipo = "ALUNOS";
            case "PROFESSORES" -> alvoTipo = "PROFESSORES";
            default -> alvoTipo = "GERAL";
        }
        
        // Converte autorId String para Long (pega do usuário logado)
        Long autorIdLong = 1L; // TODO: pegar do contexto de autenticação
        
        // Salva no banco de dados
        AvisoEntity entity = new AvisoEntity(
            request.getTitulo(),
            request.getConteudo(),
            autorIdLong,
            alvoTipo
        );
        AvisoEntity saved = avisoJpaRepository.save(entity);
        
        // Observer Pattern: Notifica sobre novo aviso
        observers.forEach(obs -> obs.onAvisoPublicado(
            avisoId, 
            request.getTitulo(),
            escopo
        ));
        
        AvisoResponse response = new AvisoResponse();
        response.setId(String.valueOf(saved.getId()));
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
        // Converte IDs para Long com tratamento de erros
        Long avisoId;
        Long usuarioId;
        
        try {
            avisoId = Long.parseLong(request.getAvisoId());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            if (request.getUsuarioId() == null || request.getUsuarioId().equals("undefined") || request.getUsuarioId().equals("null")) {
                usuarioId = 1L; // Admin padrão
            } else {
                usuarioId = Long.parseLong(request.getUsuarioId());
            }
        } catch (NumberFormatException e) {
            usuarioId = 1L; // Admin padrão em caso de erro
        }
        
        // Registra leitura se ainda não foi lida
        if (!leituraJpaRepository.existsByAvisoIdAndUsuarioId(avisoId, usuarioId)) {
            AvisoLeituraEntity leitura = new AvisoLeituraEntity(avisoId, usuarioId);
            leituraJpaRepository.save(leitura);
        }
        
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
        
        // Trata casos de usuarioId inválido
        Long usuarioIdLong;
        try {
            if (usuarioId == null || usuarioId.equals("undefined") || usuarioId.equals("null")) {
                usuarioIdLong = 1L; // Admin padrão
            } else {
                usuarioIdLong = Long.parseLong(usuarioId);
            }
        } catch (NumberFormatException e) {
            usuarioIdLong = 1L; // Admin padrão em caso de erro
        }
        
        // Busca IDs dos avisos já lidos pelo usuário
        List<Long> avisosLidos = leituraJpaRepository.findAvisosLidosByUsuarioId(usuarioIdLong);
        
        // Busca todos os avisos e filtra os não lidos
        List<AvisoResponse> naoLidos = new ArrayList<>();
        
        List<AvisoEntity> entities = avisoJpaRepository.findAll();
        entities.forEach(entity -> {
            // Só adiciona se NÃO foi lido
            if (!avisosLidos.contains(entity.getId())) {
                AvisoResponse resp = new AvisoResponse();
                resp.setId(String.valueOf(entity.getId()));
                resp.setTitulo(entity.getTitulo());
                resp.setConteudo(entity.getConteudo());
                resp.setAutorId(String.valueOf(entity.getAutorId()));
                resp.setEscopo(entity.getAlvoTipo());
                resp.setLido(false);
                naoLidos.add(resp);
            }
        });
        
        return ResponseEntity.ok(naoLidos);
    }
    
    /**
     * GET /api/avisos/historico?usuarioId=X - Listar histórico completo de avisos
     */
    @GetMapping("/historico")
    public ResponseEntity<List<AvisoResponse>> listarHistorico(
            @RequestParam String usuarioId) {
        
        // Trata casos de usuarioId inválido
        Long usuarioIdLong;
        try {
            if (usuarioId == null || usuarioId.equals("undefined") || usuarioId.equals("null")) {
                usuarioIdLong = 1L; // Admin padrão
            } else {
                usuarioIdLong = Long.parseLong(usuarioId);
            }
        } catch (NumberFormatException e) {
            usuarioIdLong = 1L; // Admin padrão em caso de erro
        }
        
        // Busca IDs dos avisos já lidos pelo usuário
        List<Long> avisosLidos = leituraJpaRepository.findAvisosLidosByUsuarioId(usuarioIdLong);
        
        // Busca todos os avisos
        List<AvisoResponse> historico = new ArrayList<>();
        
        List<AvisoEntity> entities = avisoJpaRepository.findAll();
        entities.forEach(entity -> {
            AvisoResponse resp = new AvisoResponse();
            resp.setId(String.valueOf(entity.getId()));
            resp.setTitulo(entity.getTitulo());
            resp.setConteudo(entity.getConteudo());
            resp.setAutorId(String.valueOf(entity.getAutorId()));
            resp.setEscopo(entity.getAlvoTipo());
            resp.setLido(avisosLidos.contains(entity.getId()));
            historico.add(resp);
        });
        
        return ResponseEntity.ok(historico);
    }
    
    /**
     * DELETE /api/avisos/{id} - Excluir aviso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAviso(@PathVariable String id) {
        avisoJpaRepository.deleteById(Long.parseLong(id));
        avisosArmazenados.remove(id);
        escoposArmazenados.remove(id);
        return ResponseEntity.ok().build();
    }
}
