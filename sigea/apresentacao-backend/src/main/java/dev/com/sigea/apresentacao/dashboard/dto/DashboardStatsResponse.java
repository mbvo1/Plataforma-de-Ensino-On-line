package dev.com.sigea.apresentacao.dashboard.dto;

public class DashboardStatsResponse {
    
    private long totalAlunos;
    private long totalProfessores;
    private long totalDisciplinas;
    private long totalTurmas;
    
    public DashboardStatsResponse() {
    }
    
    public long getTotalAlunos() {
        return totalAlunos;
    }
    
    public void setTotalAlunos(long totalAlunos) {
        this.totalAlunos = totalAlunos;
    }
    
    public long getTotalProfessores() {
        return totalProfessores;
    }
    
    public void setTotalProfessores(long totalProfessores) {
        this.totalProfessores = totalProfessores;
    }
    
    public long getTotalDisciplinas() {
        return totalDisciplinas;
    }
    
    public void setTotalDisciplinas(long totalDisciplinas) {
        this.totalDisciplinas = totalDisciplinas;
    }
    
    public long getTotalTurmas() {
        return totalTurmas;
    }
    
    public void setTotalTurmas(long totalTurmas) {
        this.totalTurmas = totalTurmas;
    }
}
