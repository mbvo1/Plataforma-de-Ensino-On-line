package dev.com.sigea.apresentacao.usuarios_admin.dto;

public class HistoricoDisciplinaResponse {
    private Long id;
    private String nomeDisciplina;
    private String periodoLetivo;
    private String status;

    public HistoricoDisciplinaResponse(Long id, String nomeDisciplina, String periodoLetivo, String status) {
        this.id = id;
        this.nomeDisciplina = nomeDisciplina;
        this.periodoLetivo = periodoLetivo;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public String getPeriodoLetivo() {
        return periodoLetivo;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public void setPeriodoLetivo(String periodoLetivo) {
        this.periodoLetivo = periodoLetivo;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
