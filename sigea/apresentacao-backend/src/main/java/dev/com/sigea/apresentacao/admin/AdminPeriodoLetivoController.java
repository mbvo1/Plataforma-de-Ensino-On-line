package dev.com.sigea.apresentacao.admin;

import dev.com.sigea.aplicacao.periodoletivo.CriarPeriodoLetivoCommand;
import dev.com.sigea.aplicacao.periodoletivo.CriarPeriodoLetivoUseCase;
import dev.com.sigea.aplicacao.periodoletivo.PeriodoLetivoResumo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestão de períodos letivos pelo ADMIN.
 */
@RestController
@RequestMapping("/api/admin/periodos-letivos")
@CrossOrigin(origins = "*")
public class AdminPeriodoLetivoController {
    
    private final CriarPeriodoLetivoUseCase criarPeriodoLetivoUseCase;
    
    public AdminPeriodoLetivoController(CriarPeriodoLetivoUseCase criarPeriodoLetivoUseCase) {
        this.criarPeriodoLetivoUseCase = criarPeriodoLetivoUseCase;
    }
    
    @PostMapping
    public ResponseEntity<PeriodoLetivoResumo> criarPeriodoLetivo(@RequestBody CriarPeriodoLetivoCommand command) {
        try {
            PeriodoLetivoResumo periodo = criarPeriodoLetivoUseCase.executar(command);
            return ResponseEntity.ok(periodo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

