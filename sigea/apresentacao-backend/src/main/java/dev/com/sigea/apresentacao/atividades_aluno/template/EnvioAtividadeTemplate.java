package dev.com.sigea.apresentacao.atividades_aluno.template;

import java.util.List;

/**
 * Template Method Pattern - Fluxo de envio de atividade
 */
public abstract class EnvioAtividadeTemplate {
    
    /**
     * Template Method - Define o fluxo completo
     */
    public final String enviarAtividade(String atividadeId, String alunoId, List<String> arquivos) {
        validarPrazo(atividadeId);
        validarArquivos(arquivos);
        
        String envioId = salvarEnvio(atividadeId, alunoId, arquivos);
        
        verificarBloqueio(envioId);
        
        notificarProfessor(atividadeId, alunoId);
        
        return envioId;
    }
    
    /**
     * Hook - Valida se está dentro do prazo
     */
    protected abstract void validarPrazo(String atividadeId);
    
    /**
     * Hook - Valida arquivos enviados
     */
    protected void validarArquivos(List<String> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo foi enviado");
        }
    }
    
    /**
     * Hook - Salva o envio
     */
    protected abstract String salvarEnvio(String atividadeId, String alunoId, List<String> arquivos);
    
    /**
     * Hook - Verifica se está bloqueado após correção
     */
    protected abstract void verificarBloqueio(String envioId);
    
    /**
     * Hook - Notifica o professor
     */
    protected void notificarProfessor(String atividadeId, String alunoId) {
        System.out.println("📧 Notificando professor sobre envio - Atividade: " + atividadeId);
    }
}
