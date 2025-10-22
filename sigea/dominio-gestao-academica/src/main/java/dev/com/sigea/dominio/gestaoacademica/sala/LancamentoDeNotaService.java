package dev.com.sigea.dominio.gestaoacademica.sala;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

public class LancamentoDeNotaService {

    private final SalaRepository salaRepository;

    public LancamentoDeNotaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void lancarNota(UsuarioId alunoId, SalaId salaId, String avaliacao, Nota nota) {
        // Buscar a sala
        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada."));

        // Lançar a nota (a lógica de negócio está na entidade Sala)
        sala.lancarNota(alunoId, avaliacao, nota);

        // Salvar o estado da sala
        salaRepository.salvar(sala);
    }
}
