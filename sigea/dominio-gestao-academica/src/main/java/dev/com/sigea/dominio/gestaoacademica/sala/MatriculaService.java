package dev.com.sigea.dominio.gestaoacademica.sala;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

public class MatriculaService {

    private final SalaRepository salaRepository;

    public MatriculaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void realizarMatricula(UsuarioId alunoId, SalaId salaId) {
        // Buscar a sala
        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada."));

        // Realizar a matrícula (a lógica de negócio está na entidade Sala)
        sala.matricular(alunoId);

        // Salvar o estado da sala
        salaRepository.salvar(sala);
    }
}
