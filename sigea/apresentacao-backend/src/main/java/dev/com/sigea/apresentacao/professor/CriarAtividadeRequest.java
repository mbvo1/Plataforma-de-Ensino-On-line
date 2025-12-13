package dev.com.sigea.apresentacao.professor;

import java.time.LocalDateTime;

public class CriarAtividadeRequest {
    private String titulo;
    private String descricao;
    private String arquivoPath;
    private String arquivoConteudo;
    private LocalDateTime prazo;

    public CriarAtividadeRequest() {}

    // getters and setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getArquivoPath() { return arquivoPath; }
    public void setArquivoPath(String arquivoPath) { this.arquivoPath = arquivoPath; }
    public String getArquivoConteudo() { return arquivoConteudo; }
    public void setArquivoConteudo(String arquivoConteudo) { this.arquivoConteudo = arquivoConteudo; }
    public LocalDateTime getPrazo() { return prazo; }
    public void setPrazo(LocalDateTime prazo) { this.prazo = prazo; }
}
