package dev.com.sigea.apresentacao.foruns.proxy;

import dev.com.sigea.apresentacao.foruns.observer.ForumObserver;
import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.forum.Topico;
import dev.com.sigea.dominio.forum.TopicoId;
import dev.com.sigea.dominio.forum.TopicoRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Proxy Pattern - Implementação real do serviço de fórum
 * Integrado com Observer Pattern para notificações
 */
public class ForumServiceReal implements ForumService {
    
    private final TopicoRepository topicoRepository;
    private final List<ForumObserver> observers = new ArrayList<>();
    
    public ForumServiceReal(TopicoRepository topicoRepository) {
        this.topicoRepository = topicoRepository;
    }
    
    public void adicionarObserver(ForumObserver observer) {
        observers.add(observer);
    }
    
    @Override
    public Topico criarTopico(DisciplinaId disciplinaId, UsuarioId autorId, String titulo, String conteudo, String arquivoPath) {
        TopicoId novoId = new TopicoId(UUID.randomUUID().toString());
        Topico topico = new Topico(novoId, disciplinaId, titulo, conteudo, autorId, arquivoPath);
        topicoRepository.salvar(topico);
        
        // Notifica observers sobre novo tópico
        observers.forEach(obs -> obs.onNovoTopicoCriado(topico));
        
        return topico;
    }
    
    @Override
    public List<Topico> listarTopicos(DisciplinaId disciplinaId, UsuarioId usuarioId) {
        return topicoRepository.listarPorDisciplina(disciplinaId);
    }
    
    @Override
    public void responderTopico(String topicoId, UsuarioId autorId, String conteudo) {
        System.out.println("Resposta criada no tópico " + topicoId);
        
        // Notifica observers sobre nova resposta
        observers.forEach(obs -> obs.onNovaResposta(topicoId, conteudo));
    }
    
    @Override
    public void excluirTopico(String topicoId, UsuarioId usuarioId) {
        topicoRepository.excluir(new TopicoId(topicoId));
        System.out.println("Tópico " + topicoId + " excluído por " + usuarioId);
    }
}
