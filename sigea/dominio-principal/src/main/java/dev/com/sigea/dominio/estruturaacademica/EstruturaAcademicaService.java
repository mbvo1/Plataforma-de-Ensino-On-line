package dev.com.sigea.dominio.estruturaacademica;

import dev.com.sigea.dominio.disciplina.Disciplina;
import dev.com.sigea.dominio.disciplina.DisciplinaRepository;
import dev.com.sigea.dominio.periodoletivo.PeriodoLetivo;
import dev.com.sigea.dominio.periodoletivo.PeriodoLetivoRepository;
import java.util.Date;
import java.util.Optional;

public class EstruturaAcademicaService {
    private final PeriodoLetivoRepository periodoLetivoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public EstruturaAcademicaService(PeriodoLetivoRepository periodoLetivoRepository, DisciplinaRepository disciplinaRepository) {
        this.periodoLetivoRepository = periodoLetivoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public void criarPeriodoLetivo(String identificador, Date dataInicio, Date dataFim) {
        var novoPeriodo = new PeriodoLetivo(periodoLetivoRepository.proximoId(), identificador, dataInicio, dataFim);

        periodoLetivoRepository.listarTodas().forEach(existente -> {
            if (!existente.getId().equals(novoPeriodo.getId())) {
                boolean sobrepoe = !novoPeriodo.getDataInicio().after(existente.getDataFim()) && !novoPeriodo.getDataFim().before(existente.getDataInicio());
                if (sobrepoe) {
                    throw new IllegalStateException("Já existe um período letivo ativo nas datas especificadas");
                }
            }
        });
        
        periodoLetivoRepository.salvar(novoPeriodo);
    }

    public void criarDisciplina(String nome) {
        if (disciplinaRepository.buscarPorNome(nome).isPresent()) {
            throw new IllegalStateException("Já existe uma disciplina com este nome");
        }
        
        var novaDisciplina = new Disciplina(disciplinaRepository.proximoId(), nome);
        disciplinaRepository.salvar(novaDisciplina);
    }

    public Optional<PeriodoLetivo> buscarPeriodoPorIdentificador(String identificador) {
        return periodoLetivoRepository.buscarPorIdentificador(identificador);
    }

    public Optional<Disciplina> buscarDisciplinaPorNome(String nome) {
        return disciplinaRepository.buscarPorNome(nome);
    }
}