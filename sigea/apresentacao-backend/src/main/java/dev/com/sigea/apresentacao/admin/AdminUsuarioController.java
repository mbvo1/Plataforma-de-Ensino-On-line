package dev.com.sigea.apresentacao.admin;

import dev.com.sigea.aplicacao.usuario.CriarProfessorCommand;
import dev.com.sigea.aplicacao.usuario.CriarProfessorUseCase;
import dev.com.sigea.aplicacao.usuario.UsuarioResumo;
import dev.com.sigea.aplicacao.usuario.UsuarioServicoAplicacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestão de usuários pelo ADMIN.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@CrossOrigin(origins = "*")
public class AdminUsuarioController {
    
    private final CriarProfessorUseCase criarProfessorUseCase;
    private final UsuarioServicoAplicacao usuarioServico;
    
    public AdminUsuarioController(
            CriarProfessorUseCase criarProfessorUseCase,
            UsuarioServicoAplicacao usuarioServico) {
        this.criarProfessorUseCase = criarProfessorUseCase;
        this.usuarioServico = usuarioServico;
    }
    
    @PostMapping("/professores")
    public ResponseEntity<UsuarioResumo> criarProfessor(@RequestBody CriarProfessorCommand command) {
        try {
            UsuarioResumo professor = criarProfessorUseCase.executar(command);
            return ResponseEntity.ok(professor);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioResumo>> listarUsuarios() {
        return ResponseEntity.ok(usuarioServico.listarTodos());
    }
    
    @GetMapping("/professores")
    public ResponseEntity<List<UsuarioResumo>> listarProfessores() {
        return ResponseEntity.ok(usuarioServico.buscarPorPerfil("PROFESSOR"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResumo> buscarUsuario(@PathVariable Long id) {
        return usuarioServico.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

