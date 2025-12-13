package dev.com.sigea.apresentacao.professor;

import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/professor")
@CrossOrigin(origins = "*")
public class PerfilProfessorController {
    
    private final UsuarioJpaRepository usuarioJpaRepository;
    
    public PerfilProfessorController(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }
    
    /**
     * GET /api/professor/{professorId}/perfil
     * Busca dados do perfil do professor
     */
    @GetMapping("/{professorId}/perfil")
    public ResponseEntity<?> buscarPerfil(@PathVariable Long professorId) {
        try {
            Optional<UsuarioEntity> usuarioOpt = usuarioJpaRepository.findById(professorId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Professor não encontrado"));
            }
            
            UsuarioEntity usuario = usuarioOpt.get();
            
            // Verifica se é professor
            if (!"PROFESSOR".equals(usuario.getPerfil())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Usuário não é um professor"));
            }
            
            Map<String, Object> perfil = Map.of(
                "id", usuario.getId(),
                "nome", usuario.getNome() != null ? usuario.getNome() : "",
                "email", usuario.getEmail() != null ? usuario.getEmail() : "",
                "cpf", usuario.getCpf() != null ? usuario.getCpf() : ""
            );
            
            return ResponseEntity.ok(perfil);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao buscar perfil: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/professor/{professorId}/perfil
     * Atualiza dados do perfil do professor
     */
    @PutMapping("/{professorId}/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @PathVariable Long professorId,
            @RequestBody AtualizarPerfilRequest request) {
        try {
            Optional<UsuarioEntity> usuarioOpt = usuarioJpaRepository.findById(professorId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Professor não encontrado"));
            }
            
            UsuarioEntity usuario = usuarioOpt.get();
            
            // Verifica se é professor
            if (!"PROFESSOR".equals(usuario.getPerfil())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Usuário não é um professor"));
            }
            
            // Validações
            if (request.getNome() != null && !request.getNome().trim().isEmpty()) {
                usuario.setNome(request.getNome().trim());
            }
            
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // Verifica se email já existe em outro usuário
                Optional<UsuarioEntity> emailExistente = usuarioJpaRepository.findByEmail(request.getEmail().trim());
                if (emailExistente.isPresent() && !emailExistente.get().getId().equals(professorId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Email já está em uso"));
                }
                usuario.setEmail(request.getEmail().trim());
            }
            
            if (request.getCpf() != null) {
                // Remove formatação do CPF
                String cpfLimpo = request.getCpf().replaceAll("\\D", "");
                if (!cpfLimpo.isEmpty()) {
                    // Verifica se CPF já existe em outro usuário
                    Optional<UsuarioEntity> cpfExistente = usuarioJpaRepository.findByCpf(cpfLimpo);
                    if (cpfExistente.isPresent() && !cpfExistente.get().getId().equals(professorId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("erro", "CPF já está em uso"));
                    }
                    usuario.setCpf(cpfLimpo);
                }
            }
            
            // Atualiza senha se fornecida
            if (request.getSenha() != null && !request.getSenha().trim().isEmpty()) {
                String senhaHash = "HASH_" + request.getSenha().trim();
                usuario.setSenhaHash(senhaHash);
            }
            
            UsuarioEntity usuarioAtualizado = usuarioJpaRepository.save(usuario);
            
            Map<String, Object> resposta = Map.of(
                "id", usuarioAtualizado.getId(),
                "nome", usuarioAtualizado.getNome() != null ? usuarioAtualizado.getNome() : "",
                "email", usuarioAtualizado.getEmail() != null ? usuarioAtualizado.getEmail() : "",
                "cpf", usuarioAtualizado.getCpf() != null ? usuarioAtualizado.getCpf() : "",
                "mensagem", "Perfil atualizado com sucesso"
            );
            
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao atualizar perfil: " + e.getMessage()));
        }
    }
    
    /**
     * Classe DTO para atualização de perfil
     */
    public static class AtualizarPerfilRequest {
        private String nome;
        private String email;
        private String cpf;
        private String senha;
        
        public String getNome() {
            return nome;
        }
        
        public void setNome(String nome) {
            this.nome = nome;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getCpf() {
            return cpf;
        }
        
        public void setCpf(String cpf) {
            this.cpf = cpf;
        }
        
        public String getSenha() {
            return senha;
        }
        
        public void setSenha(String senha) {
            this.senha = senha;
        }
    }
}

