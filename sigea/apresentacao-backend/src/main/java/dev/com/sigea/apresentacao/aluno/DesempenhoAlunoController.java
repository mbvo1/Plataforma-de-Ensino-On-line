package dev.com.sigea.apresentacao.aluno;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller REST para gerenciamento de desempenho acadêmico do aluno.
 */
@RestController
@RequestMapping("/api/aluno")
@CrossOrigin(origins = "*")
public class DesempenhoAlunoController {
    
    private final MatriculaJpaRepository matriculaRepository;
    private final AvaliacaoJpaRepository avaliacaoRepository;
    private final SalaJpaRepository salaRepository;
    private final DisciplinaJpaRepository disciplinaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    
    public DesempenhoAlunoController(MatriculaJpaRepository matriculaRepository,
                                    AvaliacaoJpaRepository avaliacaoRepository,
                                    SalaJpaRepository salaRepository,
                                    DisciplinaJpaRepository disciplinaRepository,
                                    UsuarioJpaRepository usuarioRepository) {
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.salaRepository = salaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * GET /api/aluno/{alunoId}/desempenho - Busca desempenho completo do aluno
     */
    @GetMapping("/{alunoId}/desempenho")
    public ResponseEntity<?> buscarDesempenho(@PathVariable Long alunoId) {
        try {
            // Verifica se o aluno existe
            Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Aluno não encontrado"));
            }
            
            // Busca todas as matrículas ativas do aluno
            List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoIdAndStatus(alunoId, "ATIVA");
            
            List<Map<String, Object>> disciplinasDesempenho = new ArrayList<>();
            
            for (MatriculaEntity matricula : matriculas) {
                // Busca informações da sala
                Optional<SalaEntity> salaOpt = salaRepository.findById(matricula.getSalaId());
                if (salaOpt.isEmpty()) {
                    continue;
                }
                
                SalaEntity sala = salaOpt.get();
                
                // Busca informações da disciplina
                Optional<DisciplinaEntity> disciplinaOpt = disciplinaRepository.findById(sala.getDisciplinaId());
                if (disciplinaOpt.isEmpty()) {
                    continue;
                }
                
                DisciplinaEntity disciplina = disciplinaOpt.get();
                
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
                
                // Calcula média parcial
                Double mediaParcial = calcularMediaParcial(av1, av2, segundaChamada);
                
                // Calcula média final
                Double mediaFinal = null;
                if (mediaParcial != null && finalNota != null) {
                    mediaFinal = (mediaParcial + finalNota) / 2.0;
                }
                
                Map<String, Object> disciplinaData = new HashMap<>();
                disciplinaData.put("disciplinaNome", disciplina.getNome());
                disciplinaData.put("salaIdentificador", sala.getIdentificador());
                disciplinaData.put("av1", av1);
                disciplinaData.put("av2", av2);
                disciplinaData.put("segundaChamada", segundaChamada);
                disciplinaData.put("mediaParcial", mediaParcial);
                disciplinaData.put("provaFinal", finalNota);
                disciplinaData.put("mediaFinal", mediaFinal);
                disciplinaData.put("faltas", matricula.getTotalFaltas());
                
                disciplinasDesempenho.add(disciplinaData);
            }
            
            // Ordena por nome da disciplina
            disciplinasDesempenho.sort((a, b) -> 
                ((String) a.get("disciplinaNome")).compareToIgnoreCase((String) b.get("disciplinaNome"))
            );
            
            return ResponseEntity.ok(disciplinasDesempenho);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro ao buscar desempenho: " + e.getMessage()));
        }
    }
    
    /**
     * Calcula a média parcial seguindo as regras:
     * - Média = (Av1 + Av2) / 2
     * - Se Av1 preenchida, Av2 não preenchida, e Segunda Chamada preenchida,
     *   então Segunda Chamada substitui Av2
     */
    private Double calcularMediaParcial(Double av1, Double av2, Double segundaChamada) {
        Double nota1 = av1;
        Double nota2 = av2;
        
        // Se Av1 está preenchida, Av2 não está, e Segunda Chamada está preenchida,
        // então Segunda Chamada substitui Av2
        if (av1 != null && av2 == null && segundaChamada != null) {
            nota2 = segundaChamada;
        }
        
        // Calcula média
        if (nota1 != null && nota2 != null) {
            return (nota1 + nota2) / 2.0;
        } else if (nota1 != null) {
            return nota1;
        } else if (nota2 != null) {
            return nota2;
        }
        
        return null;
    }
}

