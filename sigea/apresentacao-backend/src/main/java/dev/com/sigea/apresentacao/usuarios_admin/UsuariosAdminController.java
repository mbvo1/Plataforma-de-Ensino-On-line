package dev.com.sigea.apresentacao.usuarios_admin;

import dev.com.sigea.apresentacao.usuarios_admin.dto.*;
import dev.com.sigea.apresentacao.usuarios_admin.factory.UsuarioFactory;
import dev.com.sigea.apresentacao.usuarios_admin.strategy.*;
import dev.com.sigea.infraestrutura.persistencia.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Funcionalidade 07: Usuários (Admin)
 * Padrões: Factory (criação com senha provisória) + Strategy (filtros)
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class UsuariosAdminController {
    
    private final Map<String, UsuarioResponse> usuarios = new HashMap<>();
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final PeriodoJpaRepository periodoJpaRepository;
    private final SalaJpaRepository salaJpaRepository;
    
    public UsuariosAdminController(UsuarioJpaRepository usuarioJpaRepository, 
                                   DisciplinaJpaRepository disciplinaJpaRepository,
                                   PeriodoJpaRepository periodoJpaRepository,
                                   SalaJpaRepository salaJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.periodoJpaRepository = periodoJpaRepository;
        this.salaJpaRepository = salaJpaRepository;
    }
    
    @GetMapping("/alunos")
    public ResponseEntity<List<AlunoResponse>> listarAlunos() {
        List<UsuarioEntity> alunos = usuarioJpaRepository.findAll().stream()
                .filter(usuario -> "ALUNO".equals(usuario.getPerfil()))
                .collect(Collectors.toList());
        
        List<AlunoResponse> alunosResponse = alunos.stream()
                .map(usuario -> new AlunoResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getStatus()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(alunosResponse);
    }
    
    @GetMapping("/alunos/{id}")
    public ResponseEntity<AlunoResponse> buscarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        AlunoResponse response = new AlunoResponse(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/alunos/{id}/desativar")
    public ResponseEntity<Void> desativarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        aluno.setStatus("INATIVO");
        usuarioJpaRepository.save(aluno);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/alunos/{id}/ativar")
    public ResponseEntity<Void> ativarAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity aluno = alunoOpt.get();
        aluno.setStatus("ATIVO");
        usuarioJpaRepository.save(aluno);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/alunos/{id}/historico")
    public ResponseEntity<List<HistoricoDisciplinaResponse>> buscarHistoricoAluno(@PathVariable Long id) {
        Optional<UsuarioEntity> alunoOpt = usuarioJpaRepository.findById(id);
        
        if (alunoOpt.isEmpty() || !"ALUNO".equals(alunoOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        // Por enquanto retorna lista vazia
        // Futuramente: buscar matrículas do banco de dados
        List<HistoricoDisciplinaResponse> historico = new ArrayList<>();
        
        return ResponseEntity.ok(historico);
    }
    
    @PatchMapping("/matriculas/{id}/cancelar")
    public ResponseEntity<Void> cancelarMatricula(@PathVariable Long id) {
        // Por enquanto apenas retorna sucesso
        // Futuramente: implementar lógica de cancelamento
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/professores")
    public ResponseEntity<List<AlunoResponse>> listarProfessores() {
        List<UsuarioEntity> professores = usuarioJpaRepository.findAll().stream()
                .filter(usuario -> "PROFESSOR".equals(usuario.getPerfil()))
                .collect(Collectors.toList());
        
        List<AlunoResponse> professoresResponse = professores.stream()
                .map(usuario -> new AlunoResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getStatus()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(professoresResponse);
    }
    
    @GetMapping("/professores/{id}")
    public ResponseEntity<AlunoResponse> buscarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        AlunoResponse response = new AlunoResponse(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/professores/{id}/desativar")
    public ResponseEntity<Void> desativarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setStatus("INATIVO");
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/professores/{id}/ativar")
    public ResponseEntity<Void> ativarProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setStatus("ATIVO");
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/professores")
    public ResponseEntity<?> criarProfessor(@RequestBody CriarProfessorRequest request) {
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email é obrigatório"));
        }
        if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "CPF é obrigatório"));
        }
        
        // Verifica se email já existe
        Optional<UsuarioEntity> usuarioExistente = usuarioJpaRepository.findByEmail(request.getEmail());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já cadastrado"));
        }
        
        // Cria novo professor com senha padrão "senha123" (com hash)
        UsuarioEntity novoProfessor = new UsuarioEntity(
            null,
            request.getNome(),
            request.getEmail(),
            request.getCpf(),
            "HASH_senha123", // Senha padrão com hash (consistente com AutenticacaoService)
            "PROFESSOR",
            "ATIVO"
        );
        
        UsuarioEntity professorSalvo = usuarioJpaRepository.save(novoProfessor);
        
        AlunoResponse response = new AlunoResponse(
            professorSalvo.getId(),
            professorSalvo.getNome(),
            professorSalvo.getEmail(),
            professorSalvo.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/professores/{id}")
    public ResponseEntity<?> atualizarProfessor(@PathVariable Long id, @RequestBody CriarProfessorRequest request) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email é obrigatório"));
        }
        
        // Verifica se email já existe em outro usuário
        Optional<UsuarioEntity> emailExistente = usuarioJpaRepository.findByEmail(request.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já cadastrado"));
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setNome(request.getNome());
        professor.setEmail(request.getEmail());
        
        UsuarioEntity professorAtualizado = usuarioJpaRepository.save(professor);
        
        AlunoResponse response = new AlunoResponse(
            professorAtualizado.getId(),
            professorAtualizado.getNome(),
            professorAtualizado.getEmail(),
            professorAtualizado.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/professores/{id}/resetar-senha")
    public ResponseEntity<Void> resetarSenhaProfessor(@PathVariable Long id) {
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(id);
        
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioEntity professor = professorOpt.get();
        professor.setSenhaHash("senha123"); // Senha padrão (em produção, usar hash BCrypt)
        usuarioJpaRepository.save(professor);
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/professores/{id}/desativar-antigo")
    public ResponseEntity<Void> desativarAntigo(@PathVariable String id) {
        UsuarioResponse usuario = usuarios.get(id);
        if (usuario != null) {
            usuario.setStatus("INATIVO");
        }
        return ResponseEntity.ok().build();
    }
    
    // ========== DISCIPLINAS ==========
    
    @GetMapping("/disciplinas")
    public ResponseEntity<List<DisciplinaResponse>> listarDisciplinas() {
        // Busca período ativo
        Optional<PeriodoEntity> periodoAtivoOpt = periodoJpaRepository.findByStatus("ATIVO");
        
        List<DisciplinaEntity> disciplinas;
        if (periodoAtivoOpt.isPresent()) {
            // Retorna apenas disciplinas do período ativo
            disciplinas = disciplinaJpaRepository.findByPeriodoLetivoId(periodoAtivoOpt.get().getId());
        } else {
            // Se não há período ativo, retorna lista vazia
            disciplinas = new ArrayList<>();
        }
        
        List<DisciplinaResponse> disciplinasResponse = disciplinas.stream()
                .map(disciplina -> new DisciplinaResponse(
                        disciplina.getId(),
                        disciplina.getCodigo(),
                        disciplina.getNome(),
                        disciplina.getPeriodo(),
                        disciplina.getStatus(),
                        disciplina.getSalasOfertadas(),
                        new ArrayList<>() // Não retorna pré-requisitos na listagem
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(disciplinasResponse);
    }
    
    @GetMapping("/disciplinas/{id}")
    public ResponseEntity<DisciplinaResponse> buscarDisciplina(@PathVariable Long id) {
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(id);
        
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DisciplinaEntity disciplina = disciplinaOpt.get();
        List<Long> preRequisitosIds = disciplina.getPreRequisitos().stream()
                .map(DisciplinaEntity::getId)
                .collect(Collectors.toList());
        
        DisciplinaResponse response = new DisciplinaResponse(
                disciplina.getId(),
                disciplina.getCodigo(),
                disciplina.getNome(),
                disciplina.getPeriodo(),
                disciplina.getStatus(),
                disciplina.getSalasOfertadas(),
                preRequisitosIds
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/disciplinas")
    public ResponseEntity<?> criarDisciplina(@RequestBody CriarDisciplinaRequest request) {
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getPeriodo() == null || request.getPeriodo().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Período é obrigatório"));
        }
        
        // Busca período ativo
        Optional<PeriodoEntity> periodoAtivoOpt = periodoJpaRepository.findByStatus("ATIVO");
        if (periodoAtivoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Não há período letivo ativo"));
        }
        
        // Gera código único baseado no nome
        String codigo = gerarCodigoDisciplina(request.getNome());
        
        // Verifica se código já existe
        Optional<DisciplinaEntity> codigoExistente = disciplinaJpaRepository.findByCodigo(codigo);
        int contador = 1;
        String codigoFinal = codigo;
        while (codigoExistente.isPresent()) {
            codigoFinal = codigo + contador;
            codigoExistente = disciplinaJpaRepository.findByCodigo(codigoFinal);
            contador++;
        }
        
        // Cria nova disciplina
        DisciplinaEntity novaDisciplina = new DisciplinaEntity(
                null,
                codigoFinal,
                request.getNome(),
                request.getPeriodo(),
                "ATIVO",
                0
        );
        
        // Associa ao período ativo
        novaDisciplina.setPeriodoLetivo(periodoAtivoOpt.get());
        
        // Adiciona pré-requisitos se fornecidos
        if (request.getPreRequisitos() != null && !request.getPreRequisitos().isEmpty()) {
            Set<DisciplinaEntity> preRequisitos = new HashSet<>();
            for (Long preReqId : request.getPreRequisitos()) {
                disciplinaJpaRepository.findById(preReqId).ifPresent(preRequisitos::add);
            }
            novaDisciplina.setPreRequisitos(preRequisitos);
        }
        
        DisciplinaEntity disciplinaSalva = disciplinaJpaRepository.save(novaDisciplina);
        
        List<Long> preRequisitosIds = disciplinaSalva.getPreRequisitos().stream()
                .map(DisciplinaEntity::getId)
                .collect(Collectors.toList());
        
        DisciplinaResponse response = new DisciplinaResponse(
                disciplinaSalva.getId(),
                disciplinaSalva.getCodigo(),
                disciplinaSalva.getNome(),
                disciplinaSalva.getPeriodo(),
                disciplinaSalva.getStatus(),
                disciplinaSalva.getSalasOfertadas(),
                preRequisitosIds
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/disciplinas/{id}")
    public ResponseEntity<?> atualizarDisciplina(@PathVariable Long id, @RequestBody CriarDisciplinaRequest request) {
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(id);
        
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validações básicas
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
        }
        if (request.getPeriodo() == null || request.getPeriodo().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Período é obrigatório"));
        }
        
        DisciplinaEntity disciplina = disciplinaOpt.get();
        
        // Atualiza dados básicos
        disciplina.setNome(request.getNome());
        disciplina.setPeriodo(request.getPeriodo());
        
        // Atualiza pré-requisitos
        Set<DisciplinaEntity> preRequisitos = new HashSet<>();
        if (request.getPreRequisitos() != null && !request.getPreRequisitos().isEmpty()) {
            for (Long preReqId : request.getPreRequisitos()) {
                disciplinaJpaRepository.findById(preReqId).ifPresent(preRequisitos::add);
            }
        }
        disciplina.setPreRequisitos(preRequisitos);
        
        DisciplinaEntity disciplinaAtualizada = disciplinaJpaRepository.save(disciplina);
        
        List<Long> preRequisitosIds = disciplinaAtualizada.getPreRequisitos().stream()
                .map(DisciplinaEntity::getId)
                .collect(Collectors.toList());
        
        DisciplinaResponse response = new DisciplinaResponse(
                disciplinaAtualizada.getId(),
                disciplinaAtualizada.getCodigo(),
                disciplinaAtualizada.getNome(),
                disciplinaAtualizada.getPeriodo(),
                disciplinaAtualizada.getStatus(),
                disciplinaAtualizada.getSalasOfertadas(),
                preRequisitosIds
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/disciplinas/{id}/desativar")
    public ResponseEntity<Void> desativarDisciplina(@PathVariable Long id) {
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(id);
        
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DisciplinaEntity disciplina = disciplinaOpt.get();
        disciplina.setStatus("INATIVO");
        disciplinaJpaRepository.save(disciplina);
        
        // Desativa todas as salas associadas a esta disciplina
        List<SalaEntity> salas = salaJpaRepository.findByDisciplinaId(id);
        for (SalaEntity sala : salas) {
            sala.setStatus("INATIVO");
            salaJpaRepository.save(sala);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/disciplinas/{id}/ativar")
    public ResponseEntity<Void> ativarDisciplina(@PathVariable Long id) {
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(id);
        
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DisciplinaEntity disciplina = disciplinaOpt.get();
        disciplina.setStatus("ATIVO");
        disciplinaJpaRepository.save(disciplina);
        
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/disciplinas/{id}")
    public ResponseEntity<Void> excluirDisciplina(@PathVariable Long id) {
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(id);
        
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        disciplinaJpaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/periodos/atual")
    public ResponseEntity<PeriodoResponse> buscarPeriodoAtual() {
        Optional<PeriodoEntity> periodoOpt = periodoJpaRepository.findByStatus("ATIVO");
        
        if (periodoOpt.isEmpty()) {
            // Retorna período padrão se não houver nenhum ativo
            return ResponseEntity.ok(new PeriodoResponse(null, "2025.2", "ATIVO"));
        }
        
        PeriodoEntity periodo = periodoOpt.get();
        PeriodoResponse response = new PeriodoResponse(
                periodo.getId(),
                periodo.getNome(),
                periodo.getStatus(),
                periodo.getDataInicio() != null ? periodo.getDataInicio().toString() : null,
                periodo.getDataFim() != null ? periodo.getDataFim().toString() : null,
                periodo.getInscricaoInicio() != null ? periodo.getInscricaoInicio().toString() : null,
                periodo.getInscricaoFim() != null ? periodo.getInscricaoFim().toString() : null
        );
        
        return ResponseEntity.ok(response);
    }
    
    // ========== SALAS ==========
    
    @GetMapping("/disciplinas/{disciplinaId}/salas")
    public ResponseEntity<List<SalaResponse>> listarSalas(@PathVariable Long disciplinaId) {
        List<SalaEntity> salas = salaJpaRepository.findByDisciplinaId(disciplinaId);
        
        List<SalaResponse> salasResponse = salas.stream()
                .map(this::converterParaSalaResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(salasResponse);
    }
    
    @PostMapping("/disciplinas/{disciplinaId}/salas")
    public ResponseEntity<?> criarSala(@PathVariable Long disciplinaId, @RequestBody CriarSalaRequest request) {
        try {
            // Validações básicas
            if (request.getIdentificador() == null || request.getIdentificador().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Identificador é obrigatório"));
            }
            if (request.getProfessorId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Professor é obrigatório"));
            }
            if (request.getDiasSemana() == null || request.getDiasSemana().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Dias da semana são obrigatórios"));
            }
            if (request.getVagas() == null || request.getVagas() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vagas deve ser maior que zero"));
            }
            
            // Busca disciplina
            Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(disciplinaId);
            if (disciplinaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Disciplina não encontrada"));
            }
            
            // Busca professor
            Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(request.getProfessorId());
            if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Professor inválido"));
            }
            
            // Busca período letivo ativo
            Optional<PeriodoEntity> periodoOpt = periodoJpaRepository.findByStatus("ATIVO");
            if (periodoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Não há período letivo ativo. Por favor, crie um período letivo antes de criar salas."));
            }
            
            PeriodoEntity periodoAtivo = periodoOpt.get();
            Long periodoLetivoId = periodoAtivo.getId();
            
            // Verifica se a disciplina pertence ao período ativo
            DisciplinaEntity disciplina = disciplinaOpt.get();
            if (disciplina.getPeriodoLetivo() == null || !disciplina.getPeriodoLetivo().getId().equals(periodoLetivoId)) {
                return ResponseEntity.badRequest().body(Map.of("message", 
                    "A disciplina não está associada ao período letivo ativo (" + periodoAtivo.getNome() + "). " +
                    "Por favor, edite a disciplina para associá-la ao período correto."));
            }
            
            // Formata horário como "SEG,QUA,SEX 08:30-10:30"
            String diasStr = String.join(",", request.getDiasSemana());
            String horario = diasStr + " " + request.getHorarioInicio() + "-" + request.getHorarioFim();
            
            // Cria nova sala
            SalaEntity novaSala = new SalaEntity();
            novaSala.setIdentificador(request.getIdentificador());
            novaSala.setDisciplinaId(disciplinaId);
            novaSala.setPeriodoLetivoId(periodoLetivoId);
            novaSala.setProfessorId(request.getProfessorId());
            novaSala.setHorario(horario);
            novaSala.setLimiteVagas(request.getVagas());
            
            SalaEntity salaSalva = salaJpaRepository.save(novaSala);
            
            // Atualiza contador de salas ofertadas da disciplina (apenas salas ativas)
            Long totalSalas = salaJpaRepository.countByDisciplinaIdAndStatus(disciplinaId, "ATIVO");
            disciplina.setSalasOfertadas(totalSalas.intValue());
            disciplinaJpaRepository.save(disciplina);
            
            return ResponseEntity.ok(converterParaSalaResponse(salaSalva));
        } catch (Exception e) {
            // Log do erro para debug
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao criar sala: " + e.getMessage()));
        }
    }
    
    @GetMapping("/salas/{id}")
    public ResponseEntity<SalaResponse> buscarSala(@PathVariable Long id) {
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(id);
        
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(converterParaSalaResponse(salaOpt.get()));
    }
    
    @PutMapping("/salas/{id}")
    public ResponseEntity<?> atualizarSala(@PathVariable Long id, @RequestBody CriarSalaRequest request) {
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(id);
        
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validações básicas
        if (request.getIdentificador() == null || request.getIdentificador().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Identificador é obrigatório"));
        }
        if (request.getProfessorId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Professor é obrigatório"));
        }
        if (request.getDiasSemana() == null || request.getDiasSemana().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dias da semana são obrigatórios"));
        }
        if (request.getVagas() == null || request.getVagas() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vagas deve ser maior que zero"));
        }
        
        // Busca professor
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(request.getProfessorId());
        if (professorOpt.isEmpty() || !"PROFESSOR".equals(professorOpt.get().getPerfil())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Professor inválido"));
        }
        
        // Formata horário como "SEG,QUA,SEX 08:30-10:30"
        String diasStr = String.join(",", request.getDiasSemana());
        String horario = diasStr + " " + request.getHorarioInicio() + "-" + request.getHorarioFim();
        
        SalaEntity sala = salaOpt.get();
        sala.setIdentificador(request.getIdentificador());
        sala.setProfessorId(request.getProfessorId());
        sala.setHorario(horario);
        sala.setLimiteVagas(request.getVagas());
        
        SalaEntity salaAtualizada = salaJpaRepository.save(sala);
        
        return ResponseEntity.ok(converterParaSalaResponse(salaAtualizada));
    }
    
    @DeleteMapping("/salas/{id}")
    public ResponseEntity<Void> excluirSala(@PathVariable Long id) {
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(id);
        
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SalaEntity sala = salaOpt.get();
        Long disciplinaId = sala.getDisciplinaId();
        
        salaJpaRepository.deleteById(id);
        
        // Atualiza contador de salas ofertadas da disciplina (apenas salas ativas)
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(disciplinaId);
        if (disciplinaOpt.isPresent()) {
            DisciplinaEntity disciplina = disciplinaOpt.get();
            Long totalSalas = salaJpaRepository.countByDisciplinaIdAndStatus(disciplinaId, "ATIVO");
            disciplina.setSalasOfertadas(totalSalas.intValue());
            disciplinaJpaRepository.save(disciplina);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/salas/{id}/desativar")
    public ResponseEntity<Void> desativarSala(@PathVariable Long id) {
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(id);
        
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SalaEntity sala = salaOpt.get();
        sala.setStatus("INATIVO");
        salaJpaRepository.save(sala);
        
        // Atualizar contador de salas ativas da disciplina
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(sala.getDisciplinaId());
        if (disciplinaOpt.isPresent()) {
            DisciplinaEntity disciplina = disciplinaOpt.get();
            Long salasAtivas = salaJpaRepository.countByDisciplinaIdAndStatus(disciplina.getId(), "ATIVO");
            disciplina.setSalasOfertadas(salasAtivas.intValue());
            disciplinaJpaRepository.save(disciplina);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/salas/{id}/ativar")
    public ResponseEntity<Void> ativarSala(@PathVariable Long id) {
        Optional<SalaEntity> salaOpt = salaJpaRepository.findById(id);
        
        if (salaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SalaEntity sala = salaOpt.get();
        sala.setStatus("ATIVO");
        salaJpaRepository.save(sala);
        
        // Atualizar contador de salas ativas da disciplina
        Optional<DisciplinaEntity> disciplinaOpt = disciplinaJpaRepository.findById(sala.getDisciplinaId());
        if (disciplinaOpt.isPresent()) {
            DisciplinaEntity disciplina = disciplinaOpt.get();
            Long salasAtivas = salaJpaRepository.countByDisciplinaIdAndStatus(disciplina.getId(), "ATIVO");
            disciplina.setSalasOfertadas(salasAtivas.intValue());
            disciplinaJpaRepository.save(disciplina);
        }
        
        return ResponseEntity.ok().build();
    }
    
    private SalaResponse converterParaSalaResponse(SalaEntity sala) {
        // Parse horario "SEG,QUA,SEX 08:30-10:30"
        String horario = sala.getHorario() != null ? sala.getHorario() : "";
        String[] parts = horario.split(" ");
        
        List<String> diasSemana = new ArrayList<>();
        String horarioInicio = "";
        String horarioFim = "";
        
        if (parts.length >= 2) {
            diasSemana = Arrays.asList(parts[0].split(","));
            String[] horarios = parts[1].split("-");
            if (horarios.length >= 2) {
                horarioInicio = horarios[0];
                horarioFim = horarios[1];
            }
        }
        
        // Busca nome do professor
        String professorNome = "";
        Optional<UsuarioEntity> professorOpt = usuarioJpaRepository.findById(sala.getProfessorId());
        if (professorOpt.isPresent()) {
            professorNome = professorOpt.get().getNome();
        }
        
        // Busca vagas ocupadas (count de matrículas ativas)
        int vagasOcupadas = 0; // TODO: implementar contagem de matrículas
        
        return new SalaResponse(
                sala.getId(),
                sala.getIdentificador(),
                sala.getProfessorId(),
                professorNome,
                diasSemana,
                horarioInicio,
                horarioFim,
                sala.getLimiteVagas(),
                vagasOcupadas,
                sala.getStatus() != null ? sala.getStatus() : "ATIVO"
        );
    }
    
    private String gerarCodigoDisciplina(String nome) {
        // Remove espaços e caracteres especiais, pega primeiras letras
        String[] palavras = nome.trim().toUpperCase().split("\\s+");
        StringBuilder codigo = new StringBuilder();
        
        for (String palavra : palavras) {
            if (palavra.length() > 0 && codigo.length() < 6) {
                codigo.append(palavra.charAt(0));
            }
        }
        
        // Se ficou muito curto, completa com as primeiras letras da primeira palavra
        if (codigo.length() < 3 && palavras.length > 0) {
            String primeiraPalavra = palavras[0];
            for (int i = 1; i < primeiraPalavra.length() && codigo.length() < 3; i++) {
                codigo.append(primeiraPalavra.charAt(i));
            }
        }
        
        return codigo.toString();
    }
}

