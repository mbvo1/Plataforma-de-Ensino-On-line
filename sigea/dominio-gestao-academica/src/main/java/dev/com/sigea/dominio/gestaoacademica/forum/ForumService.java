package dev.com.sigea.dominio.gestaoacademica.forum;

import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaId;
import dev.com.sigea.dominio.gestaoacademica.sala.SalaRepository;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioId;

public class ForumService {
    private final SalaRepository salaRepository;
    private final TopicoRepository topicoRepository;

    public ForumService(SalaRepository salaRepository, TopicoRepository topicoRepository) {
        this.salaRepository = salaRepository;
        this.topicoRepository = topicoRepository;
    }

    public Topico criarTopico(UsuarioId autorId, DisciplinaId disciplinaId, String titulo, String conteudo) {
        boolean isAutorizado = salaRepository.listarTodas().stream()
                .filter(sala -> sala.getDisciplinaId().equals(disciplinaId))
                .anyMatch(sala -> sala.isAlunoMatriculado(autorId));

        if (!isAutorizado) {
            throw new SecurityException("Acesso não autorizado. Você não está matriculado nesta disciplina.");
        }

        TopicoId novoId = topicoRepository.proximoId();
        Topico novoTopico = new Topico(novoId, disciplinaId, titulo, conteudo, autorId);
        topicoRepository.salvar(novoTopico);
        return novoTopico;
    }
}