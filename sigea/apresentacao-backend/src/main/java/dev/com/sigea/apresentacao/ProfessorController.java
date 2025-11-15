package dev.com.sigea.apresentacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }
}

