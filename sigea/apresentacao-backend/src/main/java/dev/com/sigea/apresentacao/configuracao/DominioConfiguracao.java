package dev.com.sigea.apresentacao.configuracao;

import dev.com.sigea.dominio.identidadeacesso.autenticacao.AutenticacaoService;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioRepository;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração dos serviços de domínio.
 */
@Configuration
public class DominioConfiguracao {
    
    @Bean
    public UsuarioService usuarioService(UsuarioRepository usuarioRepository) {
        return new UsuarioService(usuarioRepository);
    }
    
    @Bean
    public AutenticacaoService autenticacaoService(UsuarioRepository usuarioRepository) {
        return new AutenticacaoService(usuarioRepository);
    }
}

