package dev.com.sigea.apresentacao.foruns.proxy;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.usuario.UsuarioId;
import java.util.List;

/**
 * Proxy Pattern - Interface do serviço de fórum
 */
public interface ForumService {
    Topico criarTopico(DisciplinaId disciplinaId, UsuarioId autorId, String titulo, String conteudo);
    List<Topico> listarTopicos(DisciplinaId disciplinaId, UsuarioId usuarioId);
    void responderTopico(String topicoId, UsuarioId autorId, String conteudo);
}
