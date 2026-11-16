package dev.com.sigea.apresentacao.autenticacao;

import dev.com.sigea.apresentacao.autenticacao.dto.AdminLoginRequest;
import dev.com.sigea.apresentacao.autenticacao.dto.AuthResponse;
import dev.com.sigea.apresentacao.autenticacao.dto.LoginRequest;
import dev.com.sigea.apresentacao.autenticacao.dto.RegistroRequest;
import dev.com.sigea.apresentacao.seguranca.JwtUtil;
import dev.com.sigea.dominio.auditoria.NovoEventoSeguranca;
import dev.com.sigea.dominio.auditoria.ResultadoEvento;
import dev.com.sigea.dominio.auditoria.TipoEventoSeguranca;
import dev.com.sigea.dominio.usuario.AutenticacaoService;
import dev.com.sigea.dominio.usuario.Perfil;
import dev.com.sigea.dominio.usuario.Usuario;
import dev.com.sigea.dominio.usuario.UsuarioRepository;
import dev.com.sigea.infraestrutura.auditoria.RegistradorLogSeguranca;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller para autenticação de usuários
 * Endpoints: POST /api/auth/login, POST /api/auth/registro
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private static final Logger LOG = LoggerFactory.getLogger(AutenticacaoController.class);

    private final AutenticacaoService autenticacaoService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final RegistradorLogSeguranca registradorLog;

    public AutenticacaoController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil,
                                  RegistradorLogSeguranca registradorLog) {
        this.autenticacaoService = new AutenticacaoService(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.registradorLog = registradorLog;
    }

    /**
     * POST /api/auth/login - Autentica um aluno
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        try {
            Usuario usuario = autenticacaoService.autenticar(
                request.getEmail(),
                request.getSenha()
            );
            registrarLogin(true, idDe(usuario), "/api/auth/login", http, null);

            String token = jwtUtil.gerarToken(
                usuario.getId().valor().toString(),
                usuario.getEmail(),
                usuario.getPerfil().toString()
            );

            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor().toString());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Login realizado com sucesso!");
            response.setToken(token);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            registrarLogin(false, idPorEmail(request.getEmail()), "/api/auth/login", http, "credenciais_invalidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            registrarLogin(false, idPorEmail(request.getEmail()), "/api/auth/login", http, "usuario_inativo");
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
            if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("CPF é obrigatório"));
            }

            String cpfLimpo = request.getCpf().replaceAll("\\D", "");

            Usuario usuario = autenticacaoService.registrar(
                request.getNome(),
                request.getEmail(),
                cpfLimpo,
                request.getSenha(),
                Perfil.ALUNO
            );

            String token = jwtUtil.gerarToken(
                usuario.getId().valor().toString(),
                usuario.getEmail(),
                usuario.getPerfil().toString()
            );

            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor().toString());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Conta criada com sucesso!");
            response.setToken(token);

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
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginRequest request, HttpServletRequest http) {
        try {
            Usuario usuario = autenticacaoService.autenticar(
                request.getEmail(),
                request.getSenha()
            );

            if (usuario.getPerfil() != Perfil.ADMINISTRADOR) {
                // Senha certa, porta errada: conta como falha, e a mais
                // interessante para a Fase 5 (aluno tentando a tela de admin).
                registrarLogin(false, idDe(usuario), "/api/auth/login-admin", http, "perfil_nao_administrador");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Acesso negado. Apenas administradores podem fazer login aqui."));
            }

            registrarLogin(true, idDe(usuario), "/api/auth/login-admin", http, null);
            String token = jwtUtil.gerarToken(
                usuario.getId().valor().toString(),
                usuario.getEmail(),
                usuario.getPerfil().toString()
            );

            AuthResponse response = new AuthResponse();
            response.setUsuarioId(usuario.getId().valor().toString());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfil(usuario.getPerfil().toString());
            response.setMensagem("Login administrativo realizado com sucesso!");
            response.setToken(token);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            registrarLogin(false, idPorEmail(request.getEmail()), "/api/auth/login-admin", http, "credenciais_invalidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Credenciais inválidas. Verifique email, CPF e senha."));
        } catch (IllegalStateException e) {
            registrarLogin(false, idPorEmail(request.getEmail()), "/api/auth/login-admin", http, "usuario_inativo");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Fase 4: LOGIN_SUCESSO / LOGIN_FALHA no log de seguranca.
     *
     * O e-mail digitado nao vai para o log, nem em falha: o log e exportado
     * para analise, e um e-mail digitado errado costuma ser o e-mail certo
     * de outra conta ou a senha no campo errado. Quando o e-mail existe, o
     * usuario_id basta para a Fase 5 contar falhas por usuario.
     *
     * Falha ao gravar o log nao derruba o login (fail-open): um banco de log
     * com problema nao deve trancar todo mundo para fora. O erro vai para o
     * log da aplicacao.
     *
     * IP: getRemoteAddr. Atras do proxy do Railway e o IP do proxy;
     * X-Forwarded-For seria o do cliente, mas e forjavel sem configurar
     * quais proxies sao confiaveis - fica como pendencia, nao improvisado.
     */
    private void registrarLogin(boolean sucesso, Long usuarioId, String recurso, HttpServletRequest http, String motivo) {
        try {
            registradorLog.registrar(new NovoEventoSeguranca(
                sucesso ? TipoEventoSeguranca.LOGIN_SUCESSO : TipoEventoSeguranca.LOGIN_FALHA,
                usuarioId, recurso, http.getRemoteAddr(),
                sucesso ? ResultadoEvento.SUCESSO : ResultadoEvento.FALHA,
                motivo == null ? null : "{\"motivo\":\"" + motivo + "\"}"));
        } catch (RuntimeException e) {
            LOG.error("Falha ao registrar evento de login no log de seguranca", e);
        }
    }

    private Long idPorEmail(String email) {
        if (email == null) {
            return null;
        }
        try {
            return usuarioRepository.buscarPorEmail(email).map(AutenticacaoController::idDe).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long idDe(Usuario usuario) {
        try {
            return Long.valueOf(usuario.getId().valor());
        } catch (RuntimeException e) {
            return null;
        }
    }

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
