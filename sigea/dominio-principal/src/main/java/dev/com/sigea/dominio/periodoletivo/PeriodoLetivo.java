package dev.com.sigea.dominio.periodoletivo;
import java.util.Date;
import java.util.Objects;
public class PeriodoLetivo {
    private PeriodoLetivoId id;
    private String identificador;
    private Date dataInicio;
    private Date dataFim;
    private PeriodoStatus status;

    public PeriodoLetivo(PeriodoLetivoId id, String identificador, Date dataInicio, Date dataFim) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("Identificador do período letivo é obrigatório.");
        }
        if (dataFim.before(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior à data de início.");
        }
        this.id = Objects.requireNonNull(id);
        this.identificador = identificador;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = PeriodoStatus.ATIVO;
    }

    public PeriodoLetivoId getId() { return id; }
    public String getIdentificador() { return identificador; }
    public Date getDataInicio() { return dataInicio; }
    public Date getDataFim() { return dataFim; }
    public PeriodoStatus getStatus() { return status; }
}