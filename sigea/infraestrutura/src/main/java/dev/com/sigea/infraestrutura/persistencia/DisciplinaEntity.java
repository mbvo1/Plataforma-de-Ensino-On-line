package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Disciplinas")
public class DisciplinaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disciplina_id")
    private Long id;
    
    @Column(name = "codigo_disciplina", nullable = false, unique = true, length = 50)
    private String codigo;
    
    @Column(nullable = false, length = 255)
    private String nome;
    
    @Column(length = 10)
    private String periodo;
    
    @Column(nullable = false, length = 20)
    private String status = "ATIVO";
    
    @Column(name = "salas_ofertadas")
    private Integer salasOfertadas = 0;
    
    @ManyToOne
    @JoinColumn(name = "periodo_id")
    private PeriodoEntity periodoLetivo;
    
    @ManyToMany
    @JoinTable(
        name = "Disciplina_PreRequisitos",
        joinColumns = @JoinColumn(name = "disciplina_id"),
        inverseJoinColumns = @JoinColumn(name = "pre_requisito_id")
    )
    private Set<DisciplinaEntity> preRequisitos = new HashSet<>();
    
    public DisciplinaEntity() {
    }
    
    public DisciplinaEntity(Long id, String codigo, String nome, String periodo, String status, Integer salasOfertadas) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.periodo = periodo;
        this.status = status;
        this.salasOfertadas = salasOfertadas;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getSalasOfertadas() {
        return salasOfertadas;
    }
    
    public void setSalasOfertadas(Integer salasOfertadas) {
        this.salasOfertadas = salasOfertadas;
    }
    
    public PeriodoEntity getPeriodoLetivo() {
        return periodoLetivo;
    }
    
    public void setPeriodoLetivo(PeriodoEntity periodoLetivo) {
        this.periodoLetivo = periodoLetivo;
    }
    
    public Set<DisciplinaEntity> getPreRequisitos() {
        return preRequisitos;
    }
    
    public void setPreRequisitos(Set<DisciplinaEntity> preRequisitos) {
        this.preRequisitos = preRequisitos;
    }
}
