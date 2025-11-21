package dev.com.sigea.apresentacao.admin;

import dev.com.sigea.aplicacao.disciplina.CriarDisciplinaCommand;
import dev.com.sigea.aplicacao.disciplina.CriarDisciplinaUseCase;
import dev.com.sigea.aplicacao.disciplina.DisciplinaResumo;
import dev.com.sigea.aplicacao.disciplina.DisciplinaServicoAplicacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestão de disciplinas pelo ADMIN.
 */
@RestController
@RequestMapping("/api/admin/disciplinas")
@CrossOrigin(origins = "*")
public class AdminDisciplinaController {
    
    private final CriarDisciplinaUseCase criarDisciplinaUseCase;
    private final DisciplinaServicoAplicacao disciplinaServico;
    
    public AdminDisciplinaController(
            CriarDisciplinaUseCase criarDisciplinaUseCase,
            DisciplinaServicoAplicacao disciplinaServico) {
        this.criarDisciplinaUseCase = criarDisciplinaUseCase;
        this.disciplinaServico = disciplinaServico;
    }
    
    @PostMapping
    public ResponseEntity<DisciplinaResumo> criarDisciplina(@RequestBody CriarDisciplinaCommand command) {
        try {
            DisciplinaResumo disciplina = criarDisciplinaUseCase.executar(command);
            return ResponseEntity.ok(disciplina);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<DisciplinaResumo>> listarDisciplinas() {
        return ResponseEntity.ok(disciplinaServico.listarTodas());
    }
}

