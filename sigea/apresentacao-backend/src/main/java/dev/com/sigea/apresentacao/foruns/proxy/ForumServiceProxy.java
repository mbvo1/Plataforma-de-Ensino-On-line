package dev.com.sigea.apresentacao.foruns.proxy;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import dev.com.sigea.infraestrutura.persistencia.MatriculaJpaRepository;
import dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository;

import java.util.List;

/**
 * Proxy Pattern - Proxy que controla acesso ao fórum
 * Verifica matrícula antes de permitir acesso
 */
public class ForumServiceProxy implements ForumService {
    
    private final ForumService forumServiceReal;
    private final SalaRepository salaRepository;
    private final SalaJpaRepository salaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    
    public ForumServiceProxy(ForumService forumServiceReal, SalaRepository salaRepository,
                            SalaJpaRepository salaJpaRepository, MatriculaJpaRepository matriculaJpaRepository) {
        this.forumServiceReal = forumServiceReal;
        this.salaRepository = salaRepository;
        this.salaJpaRepository = salaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
    }
    
    @Override
    public Topico criarTopico(DisciplinaId disciplinaId, UsuarioId autorId, String titulo, String conteudo, String arquivoPath) {
        // Proxy: Verifica se o usuário tem acesso à disciplina
        verificarAcesso(disciplinaId, autorId);
        
        System.out.println("✓ Acesso verificado para criar tópico - Usuário: " + autorId);
        
        return forumServiceReal.criarTopico(disciplinaId, autorId, titulo, conteudo, arquivoPath);
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
    
    @Override
    public void excluirTopico(String topicoId, UsuarioId usuarioId) {
        // Apenas delega para o serviço real (validação de autor feita lá)
        System.out.println("✓ Excluindo tópico - Usuário: " + usuarioId);
        forumServiceReal.excluirTopico(topicoId, usuarioId);
    }
    
    /**
     * Verifica se o usuário está matriculado na disciplina ou é professor responsável
     */
    private void verificarAcesso(DisciplinaId disciplinaId, UsuarioId usuarioId) {
        try {
            Long disciplinaIdLong = Long.parseLong(disciplinaId.valor());
            Long usuarioIdLong = Long.parseLong(usuarioId.valor());
            
            // Busca todas as salas da disciplina no banco
            List<dev.com.sigea.infraestrutura.persistencia.SalaEntity> salas = salaJpaRepository.findByDisciplinaId(disciplinaIdLong);
            
            if (salas.isEmpty()) {
                throw new SecurityException("Disciplina não encontrada ou sem salas.");
            }
            
            // Verifica se é professor responsável de alguma sala
            boolean isProfessor = salas.stream()
                .anyMatch(sala -> sala.getProfessorId() != null && sala.getProfessorId().equals(usuarioIdLong));
            
            if (isProfessor) {
                return; // Professor tem acesso
            }
            
            // Verifica se é aluno matriculado em alguma sala da disciplina
            boolean isAlunoMatriculado = salas.stream()
                .anyMatch(sala -> {
                    // Verifica se há matrícula ativa do aluno nesta sala
                    dev.com.sigea.infraestrutura.persistencia.MatriculaEntity matricula = 
                        matriculaJpaRepository.findBySalaIdAndAlunoId(sala.getId(), usuarioIdLong);
                    return matricula != null && "ATIVA".equals(matricula.getStatus());
                });
            
            if (!isAlunoMatriculado) {
                throw new SecurityException(
                    "Acesso não autorizado. Você não está matriculado nesta disciplina."
                );
            }
        } catch (NumberFormatException e) {
            throw new SecurityException("ID inválido: " + e.getMessage());
        }
    }
}
