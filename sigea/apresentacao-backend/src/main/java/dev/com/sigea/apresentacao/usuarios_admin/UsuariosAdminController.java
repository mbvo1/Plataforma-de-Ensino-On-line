package dev.com.sigea.apresentacao.usuarios_admin;

import dev.com.sigea.apresentacao.usuarios_admin.dto.*;
import dev.com.sigea.apresentacao.usuarios_admin.factory.UsuarioFactory;
import dev.com.sigea.apresentacao.usuarios_admin.strategy.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Funcionalidade 07: Usuários (Admin)
 * Padrões: Factory (criação com senha provisória) + Strategy (filtros)
 */
@RestController
@RequestMapping("/api/admin")
public class UsuariosAdminController {
    
    private final Map<String, UsuarioResponse> usuarios = new HashMap<>();
    
    @PostMapping("/professores")
    public ResponseEntity<UsuarioResponse> criarProfessor(@RequestBody CriarProfessorRequest request) {
        // Factory Pattern: cria com senha provisória
        UsuarioResponse professor = UsuarioFactory.criarProfessor(
            request.getNome(),
            request.getEmail(),
            request.getEspecialidade()
        );
        
        usuarios.put(professor.getId(), professor);
        
        return ResponseEntity.ok(professor);
    }
    
    @GetMapping("/professores")
    public ResponseEntity<List<UsuarioResponse>> listarProfessores(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String valor) {
        
        List<UsuarioResponse> profs = new ArrayList<>(usuarios.values());
        
        // Strategy Pattern: aplica filtro
        if (filtro != null && valor != null) {
            FiltroUsuarioStrategy strategy = switch (filtro) {
                case "nome" -> new FiltroPorNomeStrategy();
                case "perfil" -> new FiltroPorPerfilStrategy();
                default -> null;
            };
            
            if (strategy != null) {
                profs = strategy.filtrar(profs, valor);
            }
        }
        
        return ResponseEntity.ok(profs);
    }
    
    @PatchMapping("/professores/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable String id) {
        UsuarioResponse usuario = usuarios.get(id);
        if (usuario != null) {
            usuario.setStatus("INATIVO");
        }
        return ResponseEntity.ok().build();
    }
}
