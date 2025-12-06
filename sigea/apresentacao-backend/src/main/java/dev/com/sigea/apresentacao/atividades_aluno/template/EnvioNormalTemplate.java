package dev.com.sigea.apresentacao.atividades_aluno.template;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Template Method Pattern - Envio normal (primeira tentativa)
 */
public class EnvioNormalTemplate extends EnvioAtividadeTemplate {
    
    @Override
    protected void validarPrazo(String atividadeId) {
        // Simulação - em produção buscaria do banco
        LocalDateTime prazo = LocalDateTime.now().plusDays(7);
        
        if (LocalDateTime.now().isAfter(prazo)) {
            System.out.println("⚠️ Envio fora do prazo - será marcado como ATRASADO");
        } else {
            System.out.println("✓ Envio dentro do prazo");
        }
    }
    
    @Override
    protected String salvarEnvio(String atividadeId, String alunoId, List<String> arquivos) {
        String envioId = UUID.randomUUID().toString();
        
        System.out.println("💾 Salvando envio:");
        System.out.println("   ID: " + envioId);
        System.out.println("   Atividade: " + atividadeId);
        System.out.println("   Aluno: " + alunoId);
        System.out.println("   Arquivos: " + arquivos.size());
        
        return envioId;
    }
    
    @Override
    protected void verificarBloqueio(String envioId) {
        // Primeira tentativa não está bloqueada
        System.out.println("✓ Envio permitido (primeira tentativa)");
    }
}
