package dev.com.sigea.apresentacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aluno")
public class AlunoController {

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }
}
