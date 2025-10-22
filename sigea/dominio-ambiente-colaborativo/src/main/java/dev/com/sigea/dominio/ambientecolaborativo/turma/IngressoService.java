package dev.com.sigea.dominio.ambientecolaborativo.turma;

import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;
import java.util.Objects;

public class IngressoService {
    private final TurmaRepository turmaRepository;

    public IngressoService(TurmaRepository turmaRepository) {
        this.turmaRepository = Objects.requireNonNull(turmaRepository);
    }

    public void ingressarAluno(CodigoAcesso codigo, UsuarioId alunoId) {
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(alunoId);

        var turma = turmaRepository.buscarPorCodigoDeAcesso(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Código da turma não encontrado. Verifique o código e tente novamente."));

        turma.ingressar(alunoId);
        turmaRepository.salvar(turma);
    }
}
