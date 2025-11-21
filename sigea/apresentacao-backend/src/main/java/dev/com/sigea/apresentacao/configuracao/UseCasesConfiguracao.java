package dev.com.sigea.apresentacao.configuracao;

import dev.com.sigea.aplicacao.autenticacao.LoginUseCase;
import dev.com.sigea.aplicacao.disciplina.CriarDisciplinaUseCase;
import dev.com.sigea.aplicacao.disciplina.DisciplinaRepositorioAplicacao;
import dev.com.sigea.aplicacao.disciplina.DisciplinaServicoAplicacao;
import dev.com.sigea.aplicacao.periodoletivo.CriarPeriodoLetivoUseCase;
import dev.com.sigea.aplicacao.usuario.CriarProfessorUseCase;
import dev.com.sigea.aplicacao.usuario.UsuarioRepositorioAplicacao;
import dev.com.sigea.aplicacao.usuario.UsuarioServicoAplicacao;
import dev.com.sigea.dominio.gestaoacademica.disciplina.DisciplinaRepository;
import dev.com.sigea.dominio.gestaoacademica.periodoletivo.PeriodoLetivoRepository;
import dev.com.sigea.dominio.identidadeacesso.autenticacao.AutenticacaoService;
import dev.com.sigea.dominio.identidadeacesso.usuario.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração dos casos de uso (Use Cases).
 */
@Configuration
public class UseCasesConfiguracao {
    
    @Bean
    public LoginUseCase loginUseCase(AutenticacaoService autenticacaoService) {
        return new LoginUseCase(autenticacaoService);
    }
    
    @Bean
    public CriarProfessorUseCase criarProfessorUseCase(UsuarioService usuarioService) {
        return new CriarProfessorUseCase(usuarioService);
    }
    
    @Bean
    public CriarDisciplinaUseCase criarDisciplinaUseCase(DisciplinaRepository disciplinaRepository) {
        return new CriarDisciplinaUseCase(disciplinaRepository);
    }
    
    @Bean
    public CriarPeriodoLetivoUseCase criarPeriodoLetivoUseCase(PeriodoLetivoRepository periodoLetivoRepository) {
        return new CriarPeriodoLetivoUseCase(periodoLetivoRepository);
    }
    
    @Bean
    public UsuarioServicoAplicacao usuarioServicoAplicacao(UsuarioRepositorioAplicacao repositorio) {
        return new UsuarioServicoAplicacao(repositorio);
    }
    
    @Bean
    public DisciplinaServicoAplicacao disciplinaServicoAplicacao(DisciplinaRepositorioAplicacao repositorio) {
        return new DisciplinaServicoAplicacao(repositorio);
    }
}

