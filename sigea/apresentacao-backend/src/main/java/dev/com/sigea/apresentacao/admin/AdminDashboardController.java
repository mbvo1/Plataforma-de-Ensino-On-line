package dev.com.sigea.apresentacao.admin;

import dev.com.sigea.aplicacao.disciplina.DisciplinaServicoAplicacao;
import dev.com.sigea.aplicacao.usuario.UsuarioServicoAplicacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para dashboard do ADMIN.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {
    
    private final UsuarioServicoAplicacao usuarioServico;
    private final DisciplinaServicoAplicacao disciplinaServico;
    
    public AdminDashboardController(
            UsuarioServicoAplicacao usuarioServico,
            DisciplinaServicoAplicacao disciplinaServico) {
        this.usuarioServico = usuarioServico;
        this.disciplinaServico = disciplinaServico;
    }
    
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalAlunos", usuarioServico.buscarPorPerfil("ALUNO").size());
        stats.put("totalProfessores", usuarioServico.buscarPorPerfil("PROFESSOR").size());
        stats.put("disciplinasAtivas", disciplinaServico.listarTodas().size());
        stats.put("turmasAtivas", 0); // Implementar quando tiver turmas
        
        return ResponseEntity.ok(stats);
    }
}

