package dev.com.sigea.apresentacao.atividades_aluno.template;

import java.util.List;
import java.util.UUID;

/**
 * Template Method Pattern - Reenvio (após correção)
 * Bloqueia se já foi corrigido
 */
public class ReenvioTemplate extends EnvioAtividadeTemplate {
    
    private final boolean jaCorrigido;
    
    public ReenvioTemplate(boolean jaCorrigido) {
        this.jaCorrigido = jaCorrigido;
    }
    
    @Override
    protected void validarPrazo(String atividadeId) {
        // Reenvio geralmente permite após prazo
        System.out.println("ℹ️ Reenvio - prazo flexível");
    }
    
    @Override
    protected String salvarEnvio(String atividadeId, String alunoId, List<String> arquivos) {
        String envioId = UUID.randomUUID().toString();
        
        System.out.println("💾 Salvando REENVIO:");
        System.out.println("   ID: " + envioId);
        System.out.println("   Atividade: " + atividadeId);
        System.out.println("   Aluno: " + alunoId);
        
        return envioId;
    }
    
    @Override
    protected void verificarBloqueio(String envioId) {
        if (jaCorrigido) {
            throw new IllegalStateException(
                "🚫 BLOQUEADO: Não é possível reenviar atividade já corrigida. " +
                "Entre em contato com o professor."
            );
        }
        
        System.out.println("✓ Reenvio permitido (ainda não corrigido)");
    }
}
