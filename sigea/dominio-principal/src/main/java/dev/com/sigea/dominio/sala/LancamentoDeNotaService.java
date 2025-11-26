package dev.com.sigea.dominio.sala;

import dev.com.sigea.dominio.usuario.UsuarioId;

public class LancamentoDeNotaService {

    private final SalaRepository salaRepository;

    public LancamentoDeNotaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public void lancarNota(UsuarioId alunoId, SalaId salaId, String avaliacao, Nota nota) {
        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada."));

        sala.lancarNota(alunoId, avaliacao, nota);

        salaRepository.salvar(sala);
    }
}