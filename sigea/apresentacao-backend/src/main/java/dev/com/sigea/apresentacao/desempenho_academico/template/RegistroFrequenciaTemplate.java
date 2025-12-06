package dev.com.sigea.apresentacao.desempenho_academico.template;

import dev.com.sigea.apresentacao.desempenho_academico.observer.DesempenhoObserver;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;

import java.util.ArrayList;
import java.util.List;

/**
 * Template Method Pattern - Implementação concreta para registro de frequência
 */
public class RegistroFrequenciaTemplate extends RegistroDesempenhoTemplate {
    
    private final Sala sala;
    private final SalaRepository salaRepository;
    private final List<DesempenhoObserver> observers = new ArrayList<>();
    
    public RegistroFrequenciaTemplate(Sala sala, SalaRepository salaRepository) {
        this.sala = sala;
        this.salaRepository = salaRepository;
    }
    
    public void adicionarObserver(DesempenhoObserver observer) {
        observers.add(observer);
    }
    
    @Override
    protected void validarValor(Object valor) {
        if (!(valor instanceof Integer)) {
            throw new IllegalArgumentException("Número de faltas deve ser um inteiro");
        }
        
        Integer faltas = (Integer) valor;
        
        if (faltas < 0) {
            throw new IllegalArgumentException("Número de faltas não pode ser negativo");
        }
    }
    
    @Override
    protected Object processarValor(Object valor) {
        return valor; // Retorna as faltas sem processamento adicional
    }
    
    @Override
    protected void persistir(UsuarioId alunoId, Object valor) {
        Integer faltas = (Integer) valor;
        
        // Busca a matrícula e atualiza as faltas
        sala.getMatriculas().stream()
            .filter(m -> m.getAlunoId().equals(alunoId))
            .findFirst()
            .ifPresent(matricula -> {
                // Atualizar faltas na matrícula
                // (assumindo que existe um método para isso)
            });
        
        salaRepository.salvar(sala);
    }
    
    @Override
    protected void notificarAluno(UsuarioId alunoId, Object valor) {
        Integer faltas = (Integer) valor;
        
        // Observer Pattern: Notifica todos os observadores
        for (DesempenhoObserver observer : observers) {
            observer.onFrequenciaRegistrada(alunoId, faltas);
        }
    }
}
