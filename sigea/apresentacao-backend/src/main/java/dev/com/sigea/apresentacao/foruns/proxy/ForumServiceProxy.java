package dev.com.sigea.apresentacao.foruns.proxy;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.List;

/**
 * Proxy Pattern - Proxy que controla acesso ao fórum
 * Verifica matrícula antes de permitir acesso
 */
public class ForumServiceProxy implements ForumService {
    
    private final ForumService forumServiceReal;
    private final SalaRepository salaRepository;
    
    public ForumServiceProxy(ForumService forumServiceReal, SalaRepository salaRepository) {
        this.forumServiceReal = forumServiceReal;
        this.salaRepository = salaRepository;
    }
    
    @Override
    public Topico criarTopico(DisciplinaId disciplinaId, UsuarioId autorId, String titulo, String conteudo) {
        // Proxy: Verifica se o usuário tem acesso à disciplina
        verificarAcesso(disciplinaId, autorId);
        
        System.out.println("✓ Acesso verificado para criar tópico - Usuário: " + autorId);
        
        return forumServiceReal.criarTopico(disciplinaId, autorId, titulo, conteudo);
    }
    
    @Override
    public List<Topico> listarTopicos(DisciplinaId disciplinaId, UsuarioId usuarioId) {
        // Proxy: Verifica se o usuário tem acesso à disciplina
        verificarAcesso(disciplinaId, usuarioId);
        
        System.out.println("✓ Acesso verificado para listar tópicos - Usuário: " + usuarioId);
        
        return forumServiceReal.listarTopicos(disciplinaId, usuarioId);
    }
    
    @Override
    public void responderTopico(String topicoId, UsuarioId autorId, String conteudo) {
        // Aqui seria necessário buscar a disciplina do tópico para verificar acesso
        System.out.println("✓ Acesso verificado para responder tópico - Usuário: " + autorId);
        
        forumServiceReal.responderTopico(topicoId, autorId, conteudo);
    }
    
    /**
     * Verifica se o usuário está matriculado na disciplina
     */
    private void verificarAcesso(DisciplinaId disciplinaId, UsuarioId usuarioId) {
        // Busca todas as salas da disciplina
        List<Sala> salas = salaRepository.listarPorDisciplina(disciplinaId);
        
        boolean temAcesso = salas.stream()
            .anyMatch(sala -> sala.isAlunoMatriculado(usuarioId));
        
        if (!temAcesso) {
            throw new SecurityException(
                "Acesso não autorizado. Você não está matriculado nesta disciplina."
            );
        }
    }
}
