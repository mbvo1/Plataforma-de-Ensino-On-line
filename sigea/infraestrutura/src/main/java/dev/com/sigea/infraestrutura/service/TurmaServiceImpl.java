package dev.com.sigea.infraestrutura.service;

import dev.com.sigea.dominio.turma.TurmaRepository;
import dev.com.sigea.dominio.turma.TurmaService;
import org.springframework.stereotype.Service;

@Service
public class TurmaServiceImpl extends TurmaService {
    
    public TurmaServiceImpl(TurmaRepository turmaRepository) {
        super(turmaRepository);
    }
}
