package dev.com.sigea.apresentacao.salas;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller REST para gerenciamento de salas e alunos matriculados.
 */
@RestController
@RequestMapping("/api/salas")
@CrossOrigin(origins = "*")
public class SalasController {
    
    private final SalaJpaRepository salaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    
    public SalasController(SalaJpaRepository salaJpaRepository,
                           MatriculaJpaRepository matriculaJpaRepository,
                           AvaliacaoJpaRepository avaliacaoJpaRepository,
                           UsuarioJpaRepository usuarioJpaRepository) {
        this.salaJpaRepository = salaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }
    
    /**
     * GET /api/salas/{salaId}/alunos - Lista todos os alunos matriculados na sala com suas notas
     */
    @GetMapping("/{salaId}/alunos")
    public ResponseEntity<List<AlunoSalaResponse>> listarAlunosDaSala(
            @PathVariable Long salaId,
            @RequestParam Long professorId) {
        
        // Verifica se a sala existe e se o professor é responsável
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(salaId);
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SalaEntity sala = salaOpt.get();
        if (!sala.getProfessorId().equals(professorId)) {
            return ResponseEntity.status(403).build();
        }
        
        // Busca matrículas ativas da sala
        List<MatriculaEntity> matriculas = matriculaJpaRepository.findBySalaIdAndStatus(salaId, "ATIVA");
        
        List<AlunoSalaResponse> response = new ArrayList<>();
        
        for (MatriculaEntity matricula : matriculas) {
            AlunoSalaResponse alunoResponse = new AlunoSalaResponse();
            alunoResponse.setMatriculaId(matricula.getId());
            alunoResponse.setAlunoId(matricula.getAlunoId());
            alunoResponse.setTotalFaltas(matricula.getTotalFaltas());
            
            // Busca dados do aluno
            Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(matricula.getAlunoId());
            if (alunoOpt.isPresent()) {
                alunoResponse.setNome(alunoOpt.get().getNome());
            } else {
                alunoResponse.setNome("Aluno " + matricula.getAlunoId());
            }
            
            // Busca notas AV1 e AV2
            List<AvaliacaoEntity> avaliacoes = avaliacaoJpaRepository.findByMatriculaId(matricula.getId());
            for (AvaliacaoEntity avaliacao : avaliacoes) {
                if ("AV1".equalsIgnoreCase(avaliacao.getNomeAvaliacao())) {
                    alunoResponse.setNotaAv1(avaliacao.getValor());
                } else if ("AV2".equalsIgnoreCase(avaliacao.getNomeAvaliacao())) {
                    alunoResponse.setNotaAv2(avaliacao.getValor());
                }
            }
            
            response.add(alunoResponse);
        }
        
        return ResponseEntity.ok(response);
    }
}
