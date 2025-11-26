package dev.com.sigea.dominio.sala;

import dev.com.sigea.dominio.usuario.UsuarioId;

public class MatriculaService {

    private final SalaRepository salaRepository;

    public MatriculaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void realizarMatricula(UsuarioId alunoId, SalaId salaId) {
        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada."));

        sala.matricular(alunoId);

        salaRepository.salvar(sala);
    }
}