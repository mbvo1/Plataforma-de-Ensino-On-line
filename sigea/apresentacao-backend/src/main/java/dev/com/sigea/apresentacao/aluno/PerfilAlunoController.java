package dev.com.sigea.apresentacao.aluno;

import dev.com.sigea.dominio.usuario.Senha;
import dev.com.sigea.infraestrutura.persistencia.UsuarioEntity;
import dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/aluno")
@CrossOrigin(origins = "*")
public class PerfilAlunoController {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public PerfilAlunoController(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    /**
     * Mitigacao V-03 (IDOR): confirma que quem esta autenticado so acessa
     * o proprio registro, a menos que seja ADMIN ou PROFESSOR.
     */
    private boolean semPermissaoSobre(Long alunoId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioIdAutenticado = auth.getName();
        boolean ehAdminOuProfessor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_PROFESSOR"));
        return !ehAdminOuProfessor && !usuarioIdAutenticado.equals(alunoId.toString());
    }

    /**
     * GET /api/aluno/{alunoId}/perfil
     * Busca dados do perfil do aluno
     */
    @GetMapping("/{alunoId}/perfil")
    public ResponseEntity<?> buscarPerfil(@PathVariable Long alunoId) {
        try {
            if (semPermissaoSobre(alunoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Você não tem permissão para ver o perfil de outro aluno"));
            }

            Optional<UsuarioEntity> usuarioOpt = usuarioJpaRepository.findById(alunoId);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Aluno não encontrado"));
            }

            UsuarioEntity usuario = usuarioOpt.get();

            if (!"ALUNO".equals(usuario.getPerfil())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Usuário não é um aluno"));
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
     * PUT /api/aluno/{alunoId}/perfil
     * Atualiza dados do perfil do aluno
     */
    @PutMapping("/{alunoId}/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @PathVariable Long alunoId,
            @RequestBody AtualizarPerfilRequest request) {
        try {
            if (semPermissaoSobre(alunoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Você não tem permissão para alterar o perfil de outro aluno"));
            }

            Optional<UsuarioEntity> usuarioOpt = usuarioJpaRepository.findById(alunoId);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Aluno não encontrado"));
            }

            UsuarioEntity usuario = usuarioOpt.get();

            if (!"ALUNO".equals(usuario.getPerfil())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Usuário não é um aluno"));
            }

            if (request.getNome() != null && !request.getNome().trim().isEmpty()) {
                usuario.setNome(request.getNome().trim());
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                Optional<UsuarioEntity> emailExistente = usuarioJpaRepository.findByEmail(request.getEmail().trim());
                if (emailExistente.isPresent() && !emailExistente.get().getId().equals(alunoId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Email já está em uso"));
                }
                usuario.setEmail(request.getEmail().trim());
            }

            if (request.getCpf() != null) {
                String cpfLimpo = request.getCpf().replaceAll("\\D", "");
                if (!cpfLimpo.isEmpty()) {
                    Optional<UsuarioEntity> cpfExistente = usuarioJpaRepository.findByCpf(cpfLimpo);
                    if (cpfExistente.isPresent() && !cpfExistente.get().getId().equals(alunoId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("erro", "CPF já está em uso"));
                    }
                    usuario.setCpf(cpfLimpo);
                }
            }

            // Mitigacao V-01: senha agora passa por Argon2id, nao mais
            // pelo prefixo "HASH_" em texto claro.
            if (request.getSenha() != null && !request.getSenha().trim().isEmpty()) {
                String senhaHash = Senha.criarNova(request.getSenha().trim()).getSenhaHash();
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

    public static class AtualizarPerfilRequest {
        private String nome;
        private String email;
        private String cpf;
        private String senha;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }
}