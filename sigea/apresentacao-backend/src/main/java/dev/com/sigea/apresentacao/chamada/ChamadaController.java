package dev.com.sigea.apresentacao.chamada;

import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de chamadas.
 */
@RestController
@RequestMapping("/api/chamadas")
@CrossOrigin(origins = "*")
public class ChamadaController {
    
    private final ChamadaJpaRepository chamadaRepository;
    private final SalaJpaRepository salaRepository;
    private final MatriculaJpaRepository matriculaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    
    public ChamadaController(ChamadaJpaRepository chamadaRepository,
                           SalaJpaRepository salaRepository,
                           MatriculaJpaRepository matriculaRepository,
                           UsuarioJpaRepository usuarioRepository) {
        this.chamadaRepository = chamadaRepository;
        this.salaRepository = salaRepository;
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * GET /api/chamadas/sala/{salaId}/data/{data} - Busca chamada de uma data específica
     */
    @GetMapping("/sala/{salaId}/data/{data}")
    public ResponseEntity<?> buscarChamadaPorData(
            @PathVariable Long salaId,
            @PathVariable String data,
            @RequestParam Long professorId) {
        
        try {
            // Verifica se a sala existe e se o professor é responsável
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SalaEntity sala = salaOpt.get();
            if (!sala.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
            }
            
            // Parse da data
            LocalDate dataChamada = LocalDate.parse(data, DateTimeFormatter.ISO_DATE);
            
            // Busca matrículas ativas da sala
            List<MatriculaEntity> matriculas = matriculaRepository.findBySalaIdAndStatus(salaId, "ATIVA");
            
            // Busca chamadas existentes para esta data
            List<ChamadaEntity> chamadasExistentes = chamadaRepository.findBySalaIdAndDataChamada(salaId, dataChamada);
            Map<Long, ChamadaEntity> chamadasMap = chamadasExistentes.stream()
                .collect(Collectors.toMap(ChamadaEntity::getMatriculaId, c -> c));
            
            // Monta resposta com todos os alunos
            List<Map<String, Object>> alunosChamada = new ArrayList<>();
            
            for (MatriculaEntity matricula : matriculas) {
                Map<String, Object> alunoData = new HashMap<>();
                alunoData.put("matriculaId", matricula.getId());
                alunoData.put("alunoId", matricula.getAlunoId());
                
                // Busca nome do aluno
                Optional<UsuarioEntity> alunoOpt = usuarioRepository.findById(matricula.getAlunoId());
                alunoData.put("nome", alunoOpt.map(UsuarioEntity::getNome).orElse("Aluno " + matricula.getAlunoId()));
                
                // Busca dados da chamada se existir
                ChamadaEntity chamada = chamadasMap.get(matricula.getId());
                if (chamada != null) {
                    alunoData.put("faltaAula1", chamada.getFaltaAula1());
                    alunoData.put("faltaAula2", chamada.getFaltaAula2());
                } else {
                    alunoData.put("faltaAula1", false);
                    alunoData.put("faltaAula2", false);
                }
                
                alunosChamada.add(alunoData);
            }
            
            // Ordena por nome (ordem alfabética)
            alunosChamada.sort((a, b) -> ((String) a.get("nome")).compareToIgnoreCase((String) b.get("nome")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("salaId", salaId);
            response.put("data", data);
            response.put("alunos", alunosChamada);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao buscar chamada: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/chamadas/sala/{salaId}/data/{data} - Salva/atualiza chamada de uma data
     */
    @PostMapping("/sala/{salaId}/data/{data}")
    public ResponseEntity<?> salvarChamada(
            @PathVariable Long salaId,
            @PathVariable String data,
            @RequestParam Long professorId,
            @RequestBody SalvarChamadaRequest request) {
        
        try {
            // Verifica se a sala existe e se o professor é responsável
            Optional<SalaEntity> salaOpt = salaRepository.findById(salaId);
            if (salaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SalaEntity sala = salaOpt.get();
            if (!sala.getProfessorId().equals(professorId)) {
                return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
            }
            
            // Parse da data
            LocalDate dataChamada = LocalDate.parse(data, DateTimeFormatter.ISO_DATE);
            
            // Processa cada aluno da chamada
            for (AlunoChamadaRequest alunoReq : request.getAlunos()) {
                // Busca ou cria chamada
                Optional<ChamadaEntity> chamadaOpt = chamadaRepository.findBySalaIdAndMatriculaIdAndDataChamada(
                    salaId, 
                    alunoReq.getMatriculaId(), 
                    dataChamada
                );
                
                ChamadaEntity chamada;
                if (chamadaOpt.isPresent()) {
                    chamada = chamadaOpt.get();
                } else {
                    chamada = new ChamadaEntity();
                    chamada.setSalaId(salaId);
                    chamada.setMatriculaId(alunoReq.getMatriculaId());
                    chamada.setDataChamada(dataChamada);
                }
                
                // Busca chamada anterior para calcular diferença de faltas
                Boolean faltaAula1Anterior = chamada.getFaltaAula1();
                Boolean faltaAula2Anterior = chamada.getFaltaAula2();
                
                // Atualiza faltas
                chamada.setFaltaAula1(alunoReq.getFaltaAula1() != null && alunoReq.getFaltaAula1());
                chamada.setFaltaAula2(alunoReq.getFaltaAula2() != null && alunoReq.getFaltaAula2());
                
                // Salva chamada
                chamadaRepository.save(chamada);
                
                // Atualiza total de faltas na matrícula
                Optional<MatriculaEntity> matriculaOpt = matriculaRepository.findById(alunoReq.getMatriculaId());
                if (matriculaOpt.isPresent()) {
                    MatriculaEntity matricula = matriculaOpt.get();
                    
                    // Calcula diferença de faltas
                    int diferencaFaltas = 0;
                    if (Boolean.TRUE.equals(chamada.getFaltaAula1()) && !Boolean.TRUE.equals(faltaAula1Anterior)) {
                        diferencaFaltas++;
                    }
                    if (Boolean.TRUE.equals(chamada.getFaltaAula2()) && !Boolean.TRUE.equals(faltaAula2Anterior)) {
                        diferencaFaltas++;
                    }
                    if (Boolean.FALSE.equals(chamada.getFaltaAula1()) && Boolean.TRUE.equals(faltaAula1Anterior)) {
                        diferencaFaltas--;
                    }
                    if (Boolean.FALSE.equals(chamada.getFaltaAula2()) && Boolean.TRUE.equals(faltaAula2Anterior)) {
                        diferencaFaltas--;
                    }
                    
                    // Atualiza total de faltas
                    int novoTotal = matricula.getTotalFaltas() + diferencaFaltas;
                    if (novoTotal < 0) novoTotal = 0;
                    matricula.setTotalFaltas(novoTotal);
                    matriculaRepository.save(matricula);
                }
            }
            
            return ResponseEntity.ok(Map.of("mensagem", "Chamada salva com sucesso"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao salvar chamada: " + e.getMessage()));
        }
    }
    
    /**
     * Classe interna para request de salvar chamada
     */
    public static class SalvarChamadaRequest {
        private List<AlunoChamadaRequest> alunos;
        
        public List<AlunoChamadaRequest> getAlunos() {
            return alunos;
        }
        
        public void setAlunos(List<AlunoChamadaRequest> alunos) {
            this.alunos = alunos;
        }
    }
    
    /**
     * Classe interna para dados de chamada de um aluno
     */
    public static class AlunoChamadaRequest {
        private Long matriculaId;
        private Boolean faltaAula1;
        private Boolean faltaAula2;
        
        public Long getMatriculaId() {
            return matriculaId;
        }
        
        public void setMatriculaId(Long matriculaId) {
            this.matriculaId = matriculaId;
        }
        
        public Boolean getFaltaAula1() {
            return faltaAula1;
        }
        
        public void setFaltaAula1(Boolean faltaAula1) {
            this.faltaAula1 = faltaAula1;
        }
        
        public Boolean getFaltaAula2() {
            return faltaAula2;
        }
        
        public void setFaltaAula2(Boolean faltaAula2) {
            this.faltaAula2 = faltaAula2;
        }
    }
}

