package dev.com.sigea.apresentacao.desempenho_academico.template;

import dev.com.sigea.apresentacao.desempenho_academico.observer.DesempenhoObserver;
import dev.com.sigea.dominio.sala.Nota;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.ArrayList;
import java.util.List;

/**
 * Template Method Pattern - Implementação concreta para lançamento de notas
 */
public class RegistroNotaTemplate extends RegistroDesempenhoTemplate {
    
    private final Sala sala;
    private final String nomeAvaliacao;
    private final SalaRepository salaRepository;
    private final List<DesempenhoObserver> observers = new ArrayList<>();
    
    public RegistroNotaTemplate(Sala sala, String nomeAvaliacao, SalaRepository salaRepository) {
        this.sala = sala;
        this.nomeAvaliacao = nomeAvaliacao;
        this.salaRepository = salaRepository;
    }
    
    public void adicionarObserver(DesempenhoObserver observer) {
        observers.add(observer);
    }
    
    @Override
    protected void validarValor(Object valor) {
        if (!(valor instanceof Double)) {
            throw new IllegalArgumentException("Valor deve ser um número");
        }
        
        Double nota = (Double) valor;
        
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException(
                "Valor inválido. A nota deve ser um número entre 0 e 10");
        }
    }
    
    @Override
    protected Object processarValor(Object valor) {
        Double valorNota = (Double) valor;
        return new Nota(valorNota);
    }
    
    @Override
    protected void persistir(UsuarioId alunoId, Object valor) {
        Nota nota = (Nota) valor;
        sala.lancarNota(alunoId, nomeAvaliacao, nota);
        salaRepository.salvar(sala);
    }
    
    @Override
    protected void notificarAluno(UsuarioId alunoId, Object valor) {
        Nota nota = (Nota) valor;
        
        // Observer Pattern: Notifica todos os observadores
        for (DesempenhoObserver observer : observers) {
            observer.onNotaLancada(alunoId, nomeAvaliacao, nota.valor());
        }
    }
    
    @Override
    protected void executarPosProcessamento(UsuarioId alunoId) {
        // Hook: Pode ser usado para recalcular média, atualizar dashboard, etc.
        System.out.println("Nota registrada com sucesso para aluno: " + alunoId);
    }
}
