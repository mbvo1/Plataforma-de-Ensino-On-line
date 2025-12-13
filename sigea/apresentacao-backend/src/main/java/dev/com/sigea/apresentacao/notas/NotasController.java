package dev.com.sigea.apresentacao.notas;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller REST para gerenciamento de notas dos alunos.
 */
@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*")
public class NotasController {
    
    private final AvaliacaoJpaRepository avaliacaoRepository;
    private final SalaJpaRepository salaRepository;
    private final MatriculaJpaRepository matriculaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    
    public NotasController(AvaliacaoJpaRepository avaliacaoRepository,
                          SalaJpaRepository salaRepository,
                          MatriculaJpaRepository matriculaRepository,
                          UsuarioJpaRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.salaRepository = salaRepository;
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * GET /api/notas/sala/{salaId} - Busca todas as notas dos alunos de uma sala
     */
    @GetMapping("/sala/{salaId}")
    public ResponseEntity<?> buscarNotasDaSala(
            @PathVariable Long salaId,
            @RequestParam Long professorId) {
        
        try {
            // Verifica se a sala existe e se o professor é responsável
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SalaEntity sala = salaOpt.get();
            if (!sala.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
            }
            
            // Busca matrículas ativas da sala
            List<MatriculaEntity> matriculas = matriculaRepository.findBySalaIdAndStatus(salaId, "ATIVA");
            
            // Monta resposta com todas as notas
            List<Map<String, Object>> alunosNotas = new ArrayList<>();
            
            for (MatriculaEntity matricula : matriculas) {
                Map<String, Object> alunoData = new HashMap<>();
                alunoData.put("matriculaId", matricula.getId());
                alunoData.put("alunoId", matricula.getAlunoId());
                
                // Busca nome do aluno
                Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(matricula.getAlunoId());
                alunoData.put("nome", alunoOpt.map(UsuarioEntity::getNome).orElse("Aluno " + matricula.getAlunoId()));
                
                // Busca todas as avaliações da matrícula
                List<AvaliacaoEntity> avaliacoes = avaliacaoRepository.findByMatriculaId(matricula.getId());
                
                Double av1 = null;
                Double av2 = null;
                Double segundaChamada = null;
                Double finalNota = null;
                
                for (AvaliacaoEntity avaliacao : avaliacoes) {
                    String nomeAvaliacao = avaliacao.getNomeAvaliacao();
                    if ("AV1".equalsIgnoreCase(nomeAvaliacao)) {
                        av1 = avaliacao.getValor();
                    } else if ("AV2".equalsIgnoreCase(nomeAvaliacao)) {
                        av2 = avaliacao.getValor();
                    } else if ("SEGUNDA_CHAMADA".equalsIgnoreCase(nomeAvaliacao) || 
                               "SEGUNDA CHAMADA".equalsIgnoreCase(nomeAvaliacao)) {
                        segundaChamada = avaliacao.getValor();
                    } else if ("FINAL".equalsIgnoreCase(nomeAvaliacao)) {
                        finalNota = avaliacao.getValor();
                    }
                }
                
                alunoData.put("av1", av1);
                alunoData.put("av2", av2);
                alunoData.put("segundaChamada", segundaChamada);
                alunoData.put("final", finalNota);
                
                alunosNotas.add(alunoData);
            }
            
            // Ordena por nome (ordem alfabética)
            alunosNotas.sort((a, b) -> ((String) a.get("nome")).compareToIgnoreCase((String) b.get("nome")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("salaId", salaId);
            response.put("alunos", alunosNotas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao buscar notas: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/notas/sala/{salaId} - Salva/atualiza notas dos alunos
     */
    @PostMapping("/sala/{salaId}")
    public ResponseEntity<?> salvarNotas(
            @PathVariable Long salaId,
            @RequestParam Long professorId,
            @RequestBody SalvarNotasRequest request) {
        
        try {
            // Verifica se a sala existe e se o professor é responsável
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SalaEntity sala = salaOpt.get();
            if (!sala.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
            }
            
            // Processa cada aluno
            for (AlunoNotaRequest alunoReq : request.getAlunos()) {
                // Valida notas (0 a 10)
                if (alunoReq.getAv1() != null && (alunoReq.getAv1() < 0 || alunoReq.getAv1() > 10)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Nota AV1 deve estar entre 0 e 10"));
                }
                if (alunoReq.getAv2() != null && (alunoReq.getAv2() < 0 || alunoReq.getAv2() > 10)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Nota AV2 deve estar entre 0 e 10"));
                }
                if (alunoReq.getSegundaChamada() != null && (alunoReq.getSegundaChamada() < 0 || alunoReq.getSegundaChamada() > 10)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Nota Segunda Chamada deve estar entre 0 e 10"));
                }
                if (alunoReq.getFinal() != null && (alunoReq.getFinal() < 0 || alunoReq.getFinal() > 10)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Nota Final deve estar entre 0 e 10"));
                }
                
                // Salva/atualiza AV1
                salvarOuAtualizarAvaliacao(alunoReq.getMatriculaId(), "AV1", alunoReq.getAv1());
                
                // Salva/atualiza AV2
                salvarOuAtualizarAvaliacao(alunoReq.getMatriculaId(), "AV2", alunoReq.getAv2());
                
                // Salva/atualiza Segunda Chamada
                salvarOuAtualizarAvaliacao(alunoReq.getMatriculaId(), "SEGUNDA_CHAMADA", alunoReq.getSegundaChamada());
                
                // Salva/atualiza Final
                salvarOuAtualizarAvaliacao(alunoReq.getMatriculaId(), "FINAL", alunoReq.getFinal());
            }
            
            return ResponseEntity.ok(Map.of("mensagem", "Notas salvas com sucesso"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao salvar notas: " + e.getMessage()));
        }
    }
    
    /**
     * Salva ou atualiza uma avaliação específica
     */
    private void salvarOuAtualizarAvaliacao(Long matriculaId, String nomeAvaliacao, Double valor) {
        Optional<AvaliacaoEntity> avaliacaoOpt = avaliacaoRepository.findByMatriculaIdAndNomeAvaliacao(
            matriculaId, 
            nomeAvaliacao
        );
        
        if (valor != null) {
            // Salva ou atualiza
            AvaliacaoEntity avaliacao;
            if (avaliacaoOpt.isPresent()) {
                avaliacao = avaliacaoOpt.get();
            } else {
                avaliacao = new AvaliacaoEntity();
                avaliacao.setMatriculaId(matriculaId);
                avaliacao.setNomeAvaliacao(nomeAvaliacao);
            }
            avaliacao.setValor(valor);
            avaliacaoRepository.save(avaliacao);
        } else {
            // Remove se existir e valor for null
            if (avaliacaoOpt.isPresent()) {
                avaliacaoRepository.delete(avaliacaoOpt.get());
            }
        }
    }
    
    /**
     * Classe interna para request de salvar notas
     */
    public static class SalvarNotasRequest {
        private List<AlunoNotaRequest> alunos;
        
        public List<AlunoNotaRequest> getAlunos() {
            return alunos;
        }
        
        public void setAlunos(List<AlunoNotaRequest> alunos) {
            this.alunos = alunos;
        }
    }
    
    /**
     * Classe interna para dados de notas de um aluno
     */
    public static class AlunoNotaRequest {
        private Long matriculaId;
        private Double av1;
        private Double av2;
        private Double segundaChamada;
        private Double finalNota;
        
        public Long getMatriculaId() {
            return matriculaId;
        }
        
        public void setMatriculaId(Long matriculaId) {
            this.matriculaId = matriculaId;
        }
        
        public Double getAv1() {
            return av1;
        }
        
        public void setAv1(Double av1) {
            this.av1 = av1;
        }
        
        public Double getAv2() {
            return av2;
        }
        
        public void setAv2(Double av2) {
            this.av2 = av2;
        }
        
        public Double getSegundaChamada() {
            return segundaChamada;
        }
        
        public void setSegundaChamada(Double segundaChamada) {
            this.segundaChamada = segundaChamada;
        }
        
        public Double getFinal() {
            return finalNota;
        }
        
        public void setFinal(Double finalNota) {
            this.finalNota = finalNota;
        }
    }
}

