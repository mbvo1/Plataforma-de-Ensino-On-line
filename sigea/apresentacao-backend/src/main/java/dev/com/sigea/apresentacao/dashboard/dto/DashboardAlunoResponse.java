package dev.com.sigea.apresentacao.dashboard.dto;

public class DashboardAlunoResponse {
    
    private String nomeAluno;
    private int totalAvisos;
    private int avisosNaoLidos;
    private int totalFaltas;
    private double frequenciaPercentual;
    
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
}
