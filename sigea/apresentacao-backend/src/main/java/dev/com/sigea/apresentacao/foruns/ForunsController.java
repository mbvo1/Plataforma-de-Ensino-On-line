package dev.com.sigea.apresentacao.foruns;

import dev.com.sigea.apresentacao.foruns.dto.CriarTopicoRequest;
import dev.com.sigea.apresentacao.foruns.dto.RespostaRequest;
import dev.com.sigea.apresentacao.foruns.dto.TopicoResponse;
import dev.com.sigea.apresentacao.foruns.iterator.TopicoPaginadoIterator;
import dev.com.sigea.apresentacao.foruns.observer.DashboardProfessorObserver;
import dev.com.sigea.apresentacao.foruns.observer.MarcadorLeituraObserver;
import dev.com.sigea.apresentacao.foruns.proxy.ForumService;
import dev.com.sigea.apresentacao.foruns.proxy.ForumServiceProxy;
import dev.com.sigea.apresentacao.foruns.proxy.ForumServiceReal;
import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.forum.TopicoRepository;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de fóruns
 * 
 * Funcionalidade 03: Gerenciar Fóruns
 * Padrões implementados:
 * - Proxy Pattern: Controle de acesso por matrícula
 * - Observer Pattern: Notificações (dashboard professor, marcador leitura)
 * - Iterator Pattern: Navegação paginada de tópicos
 */
@RestController
@RequestMapping("/api/foruns")
public class ForunsController {
    
    private final ForumService forumService;
    
    public ForunsController(TopicoRepository topicoRepository, SalaRepository salaRepository) {
        // Cria serviço real e adiciona observers
        ForumServiceReal realService = new ForumServiceReal(topicoRepository);
        realService.adicionarObserver(new DashboardProfessorObserver());
        realService.adicionarObserver(new MarcadorLeituraObserver());
        
        // Envolve com Proxy para controle de acesso
        this.forumService = new ForumServiceProxy(realService, salaRepository);
    }
    
    /**
     * POST /api/foruns/topicos - Criar novo tópico
     * Proxy verifica matrícula, Observer notifica dashboard
     */
    @PostMapping("/topicos")
    public ResponseEntity<TopicoResponse> criarTopico(@RequestBody CriarTopicoRequest request) {
        Topico topico = forumService.criarTopico(
            request.getDisciplinaId(),
            request.getAutorId(),
            request.getTitulo(),
            request.getConteudo()
        );
        
        return ResponseEntity.ok(toTopicoResponse(topico));
    }
    
    /**
     * GET /api/foruns/topicos?disciplinaId=X&usuarioId=Y - Listar tópicos
     */
    @GetMapping("/topicos")
    public ResponseEntity<List<TopicoResponse>> listarTopicos(
            @RequestParam String disciplinaId,
            @RequestParam String usuarioId) {
        
        DisciplinaId discId = new DisciplinaId(disciplinaId);
        UsuarioId usrId = new UsuarioId(usuarioId);
        
        List<Topico> topicos = forumService.listarTopicos(discId, usrId);
        
        List<TopicoResponse> responses = topicos.stream()
            .map(this::toTopicoResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * GET /api/foruns/topicos/paginado?disciplinaId=X&usuarioId=Y&pagina=1&tamanho=10
     * Iterator Pattern - Navegação paginada
     */
    @GetMapping("/topicos/paginado")
    public ResponseEntity<Map<String, Object>> listarTopicosPaginado(
            @RequestParam String disciplinaId,
            @RequestParam String usuarioId,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        
        DisciplinaId discId = new DisciplinaId(disciplinaId);
        UsuarioId usrId = new UsuarioId(usuarioId);
        
        List<Topico> todosTopicos = forumService.listarTopicos(discId, usrId);
        
        // Usa Iterator Pattern para paginação
        TopicoPaginadoIterator iterator = new TopicoPaginadoIterator(todosTopicos, tamanho);
        
        // Avança até a página desejada
        for (int i = 1; i < pagina && iterator.hasNextPage(); i++) {
            iterator.nextPage();
        }
        
        List<TopicoResponse> paginaAtual = iterator.hasNextPage() ? 
            iterator.nextPage().stream()
                .map(this::toTopicoResponse)
                .collect(Collectors.toList()) : 
            List.of();
        
        Map<String, Object> response = new HashMap<>();
        response.put("topicos", paginaAtual);
        response.put("paginaAtual", pagina);
        response.put("totalPaginas", iterator.getTotalPaginas());
        response.put("temProximaPagina", iterator.hasNextPage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/foruns/topicos/{id}/respostas - Responder tópico
     * Proxy verifica acesso, Observer notifica
     */
    @PostMapping("/topicos/{topicoId}/respostas")
    public ResponseEntity<Void> responderTopico(
            @PathVariable String topicoId,
            @RequestBody RespostaRequest request) {
        
        forumService.responderTopico(
            topicoId,
            request.getAutorId(),
            request.getConteudo()
        );
        
        return ResponseEntity.ok().build();
    }
    
    private TopicoResponse toTopicoResponse(Topico topico) {
        TopicoResponse response = new TopicoResponse();
        response.setId(topico.getId().valor());
        response.setTitulo(topico.getTitulo());
        response.setConteudo(topico.getConteudo());
        response.setAutorId(topico.getAutorId().valor());
        response.setDisciplinaId(topico.getDisciplinaId().valor());
        return response;
    }
}
