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
import dev.com.sigea.dominio.forum.TopicoId;
import dev.com.sigea.dominio.forum.TopicoRepository;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import dev.com.sigea.infraestrutura.persistencia.RespostaEntity;
import dev.com.sigea.infraestrutura.persistencia.RespostaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.MatriculaJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final TopicoRepository topicoRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final RespostaJpaRepository respostaJpaRepository;
    private final SalaJpaRepository salaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    
    public ForunsController(TopicoRepository topicoRepository, SalaRepository salaRepository, 
                           UsuarioJpaRepository usuarioJpaRepository, RespostaJpaRepository respostaJpaRepository,
                           SalaJpaRepository salaJpaRepository, MatriculaJpaRepository matriculaJpaRepository) {
        this.topicoRepository = topicoRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.respostaJpaRepository = respostaJpaRepository;
        this.salaJpaRepository = salaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        
        // Cria serviço real e adiciona observers
        ForumServiceReal realService = new ForumServiceReal(topicoRepository);
        realService.adicionarObserver(new DashboardProfessorObserver());
        realService.adicionarObserver(new MarcadorLeituraObserver());
        
        // Envolve com Proxy para controle de acesso
        this.forumService = new ForumServiceProxy(realService, salaRepository, salaJpaRepository, matriculaJpaRepository);
    }
    
    /**
     * POST /api/foruns/topicos - Criar novo tópico
     * Proxy verifica matrícula, Observer notifica dashboard
     */
    @PostMapping("/topicos")
    public ResponseEntity<?> criarTopico(@RequestBody CriarTopicoRequest request) {
        try {
            Topico topico = forumService.criarTopico(
                request.getDisciplinaId(),
                request.getAutorId(),
                request.getTitulo(),
                request.getConteudo(),
                request.getArquivoPath()
            );
            
            return ResponseEntity.ok(toTopicoResponse(topico));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao criar tópico: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/foruns/topicos?disciplinaId=X&usuarioId=Y - Listar tópicos
     */
    @GetMapping("/topicos")
    public ResponseEntity<?> listarTopicos(
            @RequestParam String disciplinaId,
            @RequestParam String usuarioId) {
        try {
            DisciplinaId discId = new DisciplinaId(disciplinaId);
            UsuarioId usrId = new UsuarioId(usuarioId);
            
            List<Topico> topicos = forumService.listarTopicos(discId, usrId);
            
            List<TopicoResponse> responses = topicos.stream()
                .map(this::toTopicoResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao listar tópicos: " + e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/foruns/topicos/{id} - Excluir tópico
     * Apenas o autor pode excluir seu próprio tópico
     */
    @DeleteMapping("/topicos/{topicoId}")
    public ResponseEntity<Void> excluirTopico(
            @PathVariable String topicoId,
            @RequestParam String usuarioId) {
        
        forumService.excluirTopico(topicoId, new UsuarioId(usuarioId));
        return ResponseEntity.ok().build();
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
     * GET /api/foruns/topicos/{id} - Buscar tópico por ID
     */
    @GetMapping("/topicos/{topicoId}")
    public ResponseEntity<TopicoResponse> buscarTopico(
            @PathVariable String topicoId,
            @RequestParam String usuarioId) {
        
        Optional<Topico> topicoOpt = topicoRepository.buscarPorId(new TopicoId(topicoId));
        
        if (topicoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(toTopicoResponse(topicoOpt.get()));
    }
    
    /**
     * GET /api/foruns/topicos/{id}/respostas - Listar respostas de um tópico
     */
    @GetMapping("/topicos/{topicoId}/respostas")
    public ResponseEntity<List<RespostaResponse>> listarRespostas(
            @PathVariable String topicoId) {
        
        Long topicoIdLong = Long.parseLong(topicoId);
        List<RespostaEntity> respostas = respostaJpaRepository.findByTopicoIdOrderByDataCriacaoAsc(topicoIdLong);
        
        List<RespostaResponse> responses = respostas.stream()
            .map(this::toRespostaResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * POST /api/foruns/topicos/{id}/respostas - Responder tópico
     * Proxy verifica acesso, Observer notifica
     */
    @PostMapping("/topicos/{topicoId}/respostas")
    public ResponseEntity<RespostaResponse> responderTopico(
            @PathVariable String topicoId,
            @RequestBody RespostaRequest request) {
        
        // Persiste a resposta no banco
        RespostaEntity resposta = new RespostaEntity(
            Long.parseLong(topicoId),
            Long.parseLong(request.getAutorId().valor()),
            request.getConteudo()
        );
        
        RespostaEntity salva = respostaJpaRepository.save(resposta);
        
        // Notifica observers
        forumService.responderTopico(
            topicoId,
            request.getAutorId(),
            request.getConteudo()
        );
        
        return ResponseEntity.ok(toRespostaResponse(salva));
    }
    
    /**
     * DELETE /api/foruns/respostas/{id} - Excluir resposta/comentário
     * Apenas o autor pode excluir seu próprio comentário
     */
    @DeleteMapping("/respostas/{respostaId}")
    public ResponseEntity<Void> excluirResposta(
            @PathVariable Long respostaId,
            @RequestParam String usuarioId) {
        
        Optional<RespostaEntity> respostaOpt = respostaJpaRepository.findById(respostaId);
        
        if (respostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        RespostaEntity resposta = respostaOpt.get();
        
        // Verifica se o usuário é o autor da resposta
        if (!resposta.getAutorId().toString().equals(usuarioId)) {
            return ResponseEntity.status(403).build();
        }
        
        respostaJpaRepository.deleteById(respostaId);
        return ResponseEntity.ok().build();
    }
    
    private TopicoResponse toTopicoResponse(Topico topico) {
        TopicoResponse response = new TopicoResponse();
        response.setId(topico.getId().valor());
        response.setTitulo(topico.getTitulo());
        response.setConteudo(topico.getConteudo());
        response.setAutorId(topico.getAutorId().valor());
        response.setDisciplinaId(topico.getDisciplinaId().valor());
        response.setArquivoPath(topico.getArquivoPath());
        
        // Busca o nome do autor
        try {
            Long autorIdLong = Long.parseLong(topico.getAutorId().valor());
            Optional<UsuarioEntity> autorOpt = usuarioJpaRepository.findById(autorIdLong);
            if (autorOpt.isPresent()) {
                response.setNomeAutor(autorOpt.get().getNome());
                response.setProfessor("PROFESSOR".equals(autorOpt.get().getPerfil()));
            }
        } catch (Exception e) {
            response.setNomeAutor("Usuário " + topico.getAutorId().valor());
        }
        
        // Conta o número de respostas do tópico
        try {
            Long topicoIdLong = Long.parseLong(topico.getId().valor());
            long totalRespostas = respostaJpaRepository.countByTopicoId(topicoIdLong);
            response.setTotalRespostas((int) totalRespostas);
        } catch (Exception e) {
            response.setTotalRespostas(0);
        }
        
        return response;
    }
    
    private RespostaResponse toRespostaResponse(RespostaEntity resposta) {
        RespostaResponse response = new RespostaResponse();
        response.setId(resposta.getId());
        response.setTopicoId(resposta.getTopicoId());
        response.setAutorId(resposta.getAutorId());
        response.setConteudo(resposta.getConteudo());
        response.setVerificadaPeloProfessor(resposta.getVerificadaPeloProfessor());
        
        // Formata data
        if (resposta.getDataCriacao() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            response.setDataCriacao(resposta.getDataCriacao().format(formatter));
        }
        
        // Busca o nome do autor
        try {
            Optional<UsuarioEntity> autorOpt = usuarioJpaRepository.findById(resposta.getAutorId());
            if (autorOpt.isPresent()) {
                response.setNomeAutor(autorOpt.get().getNome());
            } else {
                response.setNomeAutor("Usuário " + resposta.getAutorId());
            }
        } catch (Exception e) {
            response.setNomeAutor("Usuário " + resposta.getAutorId());
        }
        
        return response;
    }
}
