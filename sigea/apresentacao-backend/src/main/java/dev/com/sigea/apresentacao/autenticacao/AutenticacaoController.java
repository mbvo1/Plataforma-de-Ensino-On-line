package dev.com.sigea.apresentacao.autenticacao;

import dev.com.sigea.aplicacao.autenticacao.LoginCommand;
import dev.com.sigea.aplicacao.autenticacao.LoginResponse;
import dev.com.sigea.aplicacao.autenticacao.LoginUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para autenticação.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AutenticacaoController {
    
    private final LoginUseCase loginUseCase;
    
    public AutenticacaoController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginCommand command) {
        try {
            LoginResponse response = loginUseCase.executar(command);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

