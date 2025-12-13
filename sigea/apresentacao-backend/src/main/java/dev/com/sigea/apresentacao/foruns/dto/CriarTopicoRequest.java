package dev.com.sigea.apresentacao.foruns.dto;

import dev.com.sigea.dominio.disciplina.DisciplinaId;
import dev.com.sigea.dominio.usuario.UsuarioId;
import lombok.Data;

@Data
public class CriarTopicoRequest {
    private String titulo;
    private String conteudo;
    private String disciplinaId;
    private String autorId;
    private String arquivoPath;
    
    public DisciplinaId getDisciplinaId() {
        return new DisciplinaId(disciplinaId);
    }
    
    public UsuarioId getAutorId() {
        return new UsuarioId(autorId);
    }
    
    public String getArquivoPath() {
        return arquivoPath;
    }
}
