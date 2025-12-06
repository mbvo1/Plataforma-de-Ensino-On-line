package dev.com.sigea.apresentacao.desempenho_academico;

import dev.com.sigea.apresentacao.desempenho_academico.dto.*;
import dev.com.sigea.apresentacao.desempenho_academico.observer.*;
import dev.com.sigea.apresentacao.desempenho_academico.template.*;
import dev.com.sigea.dominio.sala.Sala;
import dev.com.sigea.dominio.sala.SalaId;
import dev.com.sigea.dominio.sala.SalaRepository;
import dev.com.sigea.dominio.usuario.UsuarioId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Feature 02: Gestão de Desempenho Acadêmico
 * Padrões: Template Method (fluxo de registro) + Observer (notificações)
 */
@RestController
@RequestMapping("/api/desempenho")
public class DesempenhoAcademicoController {
    
    private final SalaRepository salaRepository;
    
    public DesempenhoAcademicoController(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }
    
    // ===== PROFESSOR: LANÇAR NOTAS =====
    
    @PostMapping("/salas/{salaId}/notas")
    public ResponseEntity<?> lancarNota(
            @PathVariable String salaId,
            @RequestBody LancarNotaRequest request) {
        try {
            Sala sala = salaRepository.buscarPorId(new SalaId(salaId))
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));
            
            // Template Method Pattern: Define o fluxo de lançamento de nota
            RegistroNotaTemplate registroNota = new RegistroNotaTemplate(
                sala, 
                request.getAvaliacao(), 
                salaRepository
            );
            
            // Observer Pattern: Adiciona observadores
            registroNota.adicionarObserver(new DashboardAlunoObserver());
            registroNota.adicionarObserver(new EmailNotificacaoObserver());
            registroNota.adicionarObserver(new AuditoriaObserver());
            
            // Executa o template method
            registroNota.registrar(
                new UsuarioId(request.getAlunoId()),
                request.getNota()
            );
            
            return ResponseEntity.ok("Nota lançada com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao lançar nota: " + e.getMessage());
        }
    }
    
    // ===== PROFESSOR: REGISTRAR FREQUÊNCIA =====
    
    @PostMapping("/salas/{salaId}/frequencia")
    public ResponseEntity<?> registrarFrequencia(
            @PathVariable String salaId,
            @RequestBody RegistrarFrequenciaRequest request) {
        try {
            Sala sala = salaRepository.buscarPorId(new SalaId(salaId))
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));
            
            // Template Method Pattern: Define o fluxo de registro de frequência
            RegistroFrequenciaTemplate registroFrequencia = new RegistroFrequenciaTemplate(
                sala,
                salaRepository
            );
            
            // Observer Pattern: Adiciona observadores
            registroFrequencia.adicionarObserver(new DashboardAlunoObserver());
            registroFrequencia.adicionarObserver(new EmailNotificacaoObserver());
            registroFrequencia.adicionarObserver(new AuditoriaObserver());
            
            // Executa o template method
            registroFrequencia.registrar(
                new UsuarioId(request.getAlunoId()),
                request.getFaltas()
            );
            
            return ResponseEntity.ok("Frequência registrada com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao registrar frequência: " + e.getMessage());
        }
    }
    
    // ===== PROFESSOR: VER ALUNOS MATRICULADOS =====
    
    @GetMapping("/salas/{salaId}/alunos")
    public ResponseEntity<?> listarAlunosMatriculados(@PathVariable String salaId) {
        try {
            Sala sala = salaRepository.buscarPorId(new SalaId(salaId))
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));
            
            List<AlunoMatriculadoResponse> alunos = new ArrayList<>();
            
            sala.getMatriculas().forEach(matricula -> {
                AlunoMatriculadoResponse response = new AlunoMatriculadoResponse();
                response.setId(matricula.getAlunoId().toString());
                // Nome e email viriam do repositório de usuários
                response.setNome("Aluno " + matricula.getAlunoId().toString());
                response.setEmail("aluno@email.com");
                alunos.add(response);
            });
            
            return ResponseEntity.ok(alunos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao listar alunos: " + e.getMessage());
        }
    }
    
    // ===== ALUNO: VER NOTAS E FALTAS =====
    
    @GetMapping("/alunos/{alunoId}/desempenho/salas/{salaId}")
    public ResponseEntity<?> verDesempenho(
            @PathVariable String alunoId,
            @PathVariable String salaId) {
        try {
            Sala sala = salaRepository.buscarPorId(new SalaId(salaId))
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));
            
            UsuarioId alunoIdObj = new UsuarioId(alunoId);
            
            // Busca a matrícula do aluno
            var matricula = sala.getMatriculas().stream()
                .filter(m -> m.getAlunoId().equals(alunoIdObj))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aluno não matriculado nesta sala"));
            
            DesempenhoResponse response = new DesempenhoResponse();
            response.setDisciplina("Disciplina " + sala.getDisciplinaId().toString());
            response.setNotas(new HashMap<>()); // Notas viriam da matrícula
            response.setFaltas(0); // Faltas viriam da matrícula
            response.setSituacaoFinal("CURSANDO");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter desempenho: " + e.getMessage());
        }
    }
    
    // ===== ALUNO: VER DESEMPENHO GERAL (Dashboard) =====
    
    @GetMapping("/alunos/{alunoId}/resumo")
    public ResponseEntity<?> verResumoDesempenho(@PathVariable String alunoId) {
        try {
            // Implementação simplificada - retorna resumo de todas as salas
            List<DesempenhoResponse> resumo = new ArrayList<>();
            
            return ResponseEntity.ok(resumo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter resumo: " + e.getMessage());
        }
    }
}
