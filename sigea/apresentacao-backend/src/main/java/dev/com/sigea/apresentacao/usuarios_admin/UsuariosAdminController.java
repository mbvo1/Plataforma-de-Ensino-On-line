package dev.com.sigea.apresentacao.usuarios_admin;

import dev.com.sigea.apresentacao.usuarios_admin.dto.*;
import dev.com.sigea.apresentacao.usuarios_admin.factory.UsuarioFactory;
import dev.com.sigea.apresentacao.usuarios_admin.strategy.*;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Funcionalidade 07: Usuários (Admin)
 * Padrões: Factory (criação com senha provisória) + Strategy (filtros)
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class UsuariosAdminController {
    
    private final Map<String, UsuarioResponse> usuarios = new HashMap<>();
    private final UsuarioJpaRepository usuarioJpaRepository;
    
    public UsuariosAdminController(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }
    
    @GetMapping("/alunos")
    public ResponseEntity<List<AlunoResponse>> listarAlunos() {
        List<UsuarioEntity> alunos = usuarioJpaRepository.findAll().stream()
                .filter(usuario -> "ALUNO".equals(usuario.getPerfil()))
                .collect(Collectors.toList());
        
        List<AlunoResponse> alunosResponse = alunos.stream()
                .map(usuario -> new AlunoResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getStatus()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(alunosResponse);
    }
    
    @GetMapping("/alunos/{id}")
    public ResponseEntity<AlunoResponse> buscarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        AlunoResponse response = new AlunoResponse(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/alunos/{id}/desativar")
    public ResponseEntity<Void> desativarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        aluno.setStatus("INATIVO");
        usuarioJpaRepository.save(aluno);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/alunos/{id}/ativar")
    public ResponseEntity<Void> ativarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        aluno.setStatus("ATIVO");
        usuarioJpaRepository.save(aluno);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/alunos/{id}/historico")
    public ResponseEntity<List<HistoricoDisciplinaResponse>> buscarHistoricoAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        // Por enquanto retorna lista vazia
        // Futuramente: buscar matrículas do banco de dados
        List<HistoricoDisciplinaResponse> historico = new ArrayList<>();
        
        return ResponseEntity.ok(historico);
    }
    
    @PatchMapping("/matriculas/{id}/cancelar")
    public ResponseEntity<Void> cancelarMatricula(@PathVariable Long id) {
        // Por enquanto apenas retorna sucesso
        // Futuramente: implementar lógica de cancelamento
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/professores")
    public ResponseEntity<List<AlunoResponse>> listarProfessores() {
        List<UsuarioEntity> professores = usuarioJpaRepository.findAll().stream()
                .filter(usuario -> "PROFESSOR".equals(usuario.getPerfil()))
                .collect(Collectors.toList());
        
        List<AlunoResponse> professoresResponse = professores.stream()
                .map(usuario -> new AlunoResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getStatus()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(professoresResponse);
    }
    
    @GetMapping("/professores/{id}")
    public ResponseEntity<AlunoResponse> buscarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        AlunoResponse response = new AlunoResponse(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/professores/{id}/desativar")
    public ResponseEntity<Void> desativarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setStatus("INATIVO");
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/professores/{id}/ativar")
    public ResponseEntity<Void> ativarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setStatus("ATIVO");
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/professores")
    public ResponseEntity<?> criarProfessor(@RequestBody CriarProfessorRequest request) {
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email é obrigatório"));
        }
        if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "CPF é obrigatório"));
        }
        
        // Verifica se email já existe
        Optional<UsuarioEntity> usuarioExistente = usuarioJpaRepository.findByEmail(request.getEmail());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já cadastrado"));
        }
        
        // Cria novo professor com senha padrão "senha123"
        UsuarioEntity novoProfessor = new UsuarioEntity(
            null,
            request.getNome(),
            request.getEmail(),
            request.getCpf(),
            "senha123", // Senha padrão (em produção, usar hash BCrypt)
            "PROFESSOR",
            "ATIVO"
        );
        
        UsuarioEntity professorSalvo = usuarioJpaRepository.save(novoProfessor);
        
        AlunoResponse response = new AlunoResponse(
            professorSalvo.getId(),
            professorSalvo.getNome(),
            professorSalvo.getEmail(),
            professorSalvo.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/professores/{id}")
    public ResponseEntity<?> atualizarProfessor(@PathVariable Long id, @RequestBody CriarProfessorRequest request) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email é obrigatório"));
        }
        
        // Verifica se email já existe em outro usuário
        Optional<UsuarioEntity> emailExistente = usuarioJpaRepository.findByEmail(request.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já cadastrado"));
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setNome(request.getNome());
        professor.setEmail(request.getEmail());
        
        UsuarioEntity professorAtualizado = usuarioJpaRepository.save(professor);
        
        AlunoResponse response = new AlunoResponse(
            professorAtualizado.getId(),
            professorAtualizado.getNome(),
            professorAtualizado.getEmail(),
            professorAtualizado.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/professores/{id}/resetar-senha")
    public ResponseEntity<Void> resetarSenhaProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setSenhaHash("senha123"); // Senha padrão (em produção, usar hash BCrypt)
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/professores/{id}/desativar-antigo")
    public ResponseEntity<Void> desativarAntigo(@PathVariable String id) {
        UsuarioResponse usuario = usuarios.get(id);
        if (usuario != null) {
            usuario.setStatus("INATIVO");
        }
        return ResponseEntity.ok().build();
    }
}

