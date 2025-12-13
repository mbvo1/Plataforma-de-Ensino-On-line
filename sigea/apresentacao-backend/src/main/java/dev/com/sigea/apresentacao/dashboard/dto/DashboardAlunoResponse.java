package dev.com.sigea.apresentacao.dashboard.dto;

import java.util.List;

public class DashboardAlunoResponse {
    
    private String nomeAluno;
    private int totalAvisos;
    private int avisosNaoLidos;
    private int totalFaltas;
    private double frequenciaPercentual;
    private List<EventoProximo> eventosProximos;
    private List<NotaResumo> notasResumo;
    
    public DashboardAlunoResponse() {
    }
    
    public String getNomeAluno() {
        return nomeAluno;
    }
    
    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }
    
    public int getTotalAvisos() {
        return totalAvisos;
    }
    
    public void setTotalAvisos(int totalAvisos) {
        this.totalAvisos = totalAvisos;
    }
    
    public int getAvisosNaoLidos() {
        return avisosNaoLidos;
    }
    
    public void setAvisosNaoLidos(int avisosNaoLidos) {
        this.avisosNaoLidos = avisosNaoLidos;
    }
    
    public int getTotalFaltas() {
        return totalFaltas;
    }
    
    public void setTotalFaltas(int totalFaltas) {
        this.totalFaltas = totalFaltas;
    }
    
    public double getFrequenciaPercentual() {
        return frequenciaPercentual;
    }
    
    public void setFrequenciaPercentual(double frequenciaPercentual) {
        this.frequenciaPercentual = frequenciaPercentual;
    }
    
    public List<EventoProximo> getEventosProximos() {
        return eventosProximos;
    }
    
    public void setEventosProximos(List<EventoProximo> eventosProximos) {
        this.eventosProximos = eventosProximos;
    }
    
    public List<NotaResumo> getNotasResumo() {
        return notasResumo;
    }
    
    public void setNotasResumo(List<NotaResumo> notasResumo) {
        this.notasResumo = notasResumo;
    }
    
    public static class EventoProximo {
        private String titulo;
        private String tipo; // EVENTO, ATIVIDADE_TURMA, etc
        private String data;
        private String disciplinaNome; // Para atividades de turma
        
        public String getTitulo() {
            return titulo;
        }
        
        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }
        
        public String getTipo() {
            return tipo;
        }
        
        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
        
        public String getData() {
            return data;
        }
        
        public void setData(String data) {
            this.data = data;
        }
        
        public String getDisciplinaNome() {
            return disciplinaNome;
        }
        
        public void setDisciplinaNome(String disciplinaNome) {
            this.disciplinaNome = disciplinaNome;
        }
    }
    
    public static class NotaResumo {
        private String disciplinaNome;
        private Double av1;
        private Double av2;
        private Double mediaParcial;
        
        public String getDisciplinaNome() {
            return disciplinaNome;
        }
        
        public void setDisciplinaNome(String disciplinaNome) {
            this.disciplinaNome = disciplinaNome;
        }
        
        public Double getAv1() {
            return av1;
        }
        
        public void setAv1(Double av1) {
            this.av1 = av1;
        }
        
        public Double getAv2() {
            return av2;
        }
        
        public void setAv2(Double av2) {
            this.av2 = av2;
        }
        
        public Double getMediaParcial() {
            return mediaParcial;
        }
        
        public void setMediaParcial(Double mediaParcial) {
            this.mediaParcial = mediaParcial;
        }
    }
}
