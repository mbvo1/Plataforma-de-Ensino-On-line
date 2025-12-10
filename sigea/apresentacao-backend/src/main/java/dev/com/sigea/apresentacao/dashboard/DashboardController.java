package dev.com.sigea.apresentacao.dashboard;

import dev.com.sigea.apresentacao.dashboard.dto.DashboardAlunoResponse;
import dev.com.sigea.apresentacao.dashboard.dto.DashboardStatsResponse;
import dev.com.sigea.apresentacao.dashboard.dto.UsuarioResumo;
import dev.com.sigea.infraestrutura.persistencia.DisciplinaEntity;
import dev.com.sigea.infraestrutura.persistencia.DisciplinaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final UsuarioJpaRepository usuarioRepository;
    private final DisciplinaJpaRepository disciplinaRepository;
    
    public DashboardController(UsuarioJpaRepository usuarioRepository, DisciplinaJpaRepository disciplinaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.disciplinaRepository = disciplinaRepository;
    }
    
    @GetMapping("/aluno/{usuarioId}")
    public ResponseEntity<DashboardAlunoResponse> getDashboardAluno(@PathVariable Long usuarioId) {
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(usuarioId);
        
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DashboardAlunoResponse response = new DashboardAlunoResponse();
        response.setNomeAluno(usuario.get().getNome());
        
        // Por enquanto, valores padrão (serão implementados quando criar as features de avisos e frequência)
        response.setTotalAvisos(0);
        response.setAvisosNaoLidos(0);
        response.setTotalFaltas(0);
        response.setFrequenciaPercentual(100.0);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        List<UsuarioEntity> todosUsuarios = usuarioRepository.findAll();
        
        long totalAlunos = todosUsuarios.stream()
            .filter(u -> "ALUNO".equals(u.getPerfil()))
            .count();
            
        long totalProfessores = todosUsuarios.stream()
            .filter(u -> "PROFESSOR".equals(u.getPerfil()))
            .count();
        
        // Contar disciplinas ativas
        List<DisciplinaEntity> todasDisciplinas = disciplinaRepository.findAll();
        long totalDisciplinas = todasDisciplinas.stream()
            .filter(d -> "ATIVO".equals(d.getStatus()))
            .count();
        
        // Por enquanto, turmas são 0 (será implementada depois)
        long totalTurmas = 0;
        
        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setTotalAlunos(totalAlunos);
        response.setTotalProfessores(totalProfessores);
        response.setTotalDisciplinas(totalDisciplinas);
        response.setTotalTurmas(totalTurmas);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ultimos-usuarios")
    public ResponseEntity<List<UsuarioResumo>> getUltimosUsuarios() {
        List<UsuarioEntity> todosUsuarios = usuarioRepository.findAll();
        
        // Pegar os últimos 5 usuários que não são administradores (ordenar por ID decrescente)
        List<UsuarioResumo> ultimos = todosUsuarios.stream()
            .filter(u -> !"ADMINISTRADOR".equals(u.getPerfil()))
            .sorted((u1, u2) -> Long.compare(u2.getId(), u1.getId()))
            .limit(5)
            .map(this::toResumo)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ultimos);
    }
    
    private UsuarioResumo toResumo(UsuarioEntity entity) {
        UsuarioResumo resumo = new UsuarioResumo();
        resumo.setNome(entity.getNome());
        resumo.setPerfil(entity.getPerfil());
        return resumo;
    }
}
