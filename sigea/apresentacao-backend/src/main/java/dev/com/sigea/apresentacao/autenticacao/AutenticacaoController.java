package dev.com.sigea.apresentacao.autenticacao;

import dev.com.sigea.apresentacao.autenticacao.dto.AdminLoginRequest;
import dev.com.sigea.apresentacao.autenticacao.dto.AuthResponse;
import dev.com.sigea.apresentacao.autenticacao.dto.LoginRequest;
import dev.com.sigea.apresentacao.autenticacao.dto.RegistroRequest;
import dev.com.sigea.dominio.usuario.AutenticacaoService;
import dev.com.sigea.dominio.usuario.Perfil;
import dev.com.sigea.dominio.usuario.Usuario;
import dev.com.sigea.dominio.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller para autenticação de usuários
 * Endpoints: POST /api/auth/login, POST /api/auth/registro
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AutenticacaoController {
    
    private final AutenticacaoService autenticacaoService;
    
    public AutenticacaoController(UsuarioRepository usuarioRepository) {
        this.autenticacaoService = new AutenticacaoService(usuarioRepository);
    }
    
    /**
     * POST /api/auth/login - Autentica um aluno
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = autenticacaoService.autenticar(
                request.getEmail(), 
                request.getSenha()
            );
            
            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Login realizado com sucesso!");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * POST /api/auth/registro - Registra um novo aluno
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroRequest request) {
        try {
            // Log para debug
            System.out.println("DEBUG - Request recebido: nome=" + request.getNome() + 
                             ", email=" + request.getEmail() + 
                             ", cpf=" + request.getCpf() + 
                             ", senha=" + (request.getSenha() != null ? "***" : "null"));
            
            // Valida se CPF foi enviado
            if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("CPF é obrigatório"));
            }
            
            // Remove qualquer formatação do CPF (deixa apenas números)
            String cpfLimpo = request.getCpf().replaceAll("\\D", "");
            
            // Registra sempre como ALUNO
            Usuario usuario = autenticacaoService.registrar(
                request.getNome(),
                request.getEmail(),
                cpfLimpo,
                request.getSenha(),
                Perfil.ALUNO
            );
            
            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Conta criada com sucesso!");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * POST /api/auth/login-admin - Autentica um administrador com email, CPF e senha
     */
    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginRequest request) {
        try {
            // Por enquanto, validação simplificada
            // TODO: Implementar validação real com CPF no banco de dados
            Usuario usuario = autenticacaoService.autenticar(
                request.getEmail(), 
                request.getSenha()
            );
            
            // Verifica se o usuário é administrador
            if (usuario.getPerfil() != Perfil.ADMINISTRADOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Acesso negado. Apenas administradores podem fazer login aqui."));
            }
            
            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Login administrativo realizado com sucesso!");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Credenciais inválidas. Verifique email, CPF e senha."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Classe interna para respostas de erro
     */
    private static class ErrorResponse {
        private final String erro;
        
        public ErrorResponse(String erro) {
            this.erro = erro;
        }
        
        public String getErro() {
            return erro;
        }
    }
}
