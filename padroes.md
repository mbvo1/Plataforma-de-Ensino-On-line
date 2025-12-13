# Padrões de Projeto Adotados no SIGEA

Este documento lista os padrões de projeto adotados no projeto SIGEA, organizados por funcionalidade, e as classes criadas e/ou alteradas por conta de sua adoção.

---

## 1. Gerenciar Conteúdo Didático (Materiais e Atividades) - Professor

### 1.1. Strategy Pattern - Validação de Deadline
**Objetivo:** Permitir diferentes estratégias de validação de prazo para atividades (obrigatório ou opcional).

**Classes:**
- `dev.com.sigea.apresentacao.conteudo_didatico.strategy.ValidacaoDeadlineStrategy` (interface)
- `dev.com.sigea.apresentacao.conteudo_didatico.strategy.DeadlineObrigatorioStrategy`
- `dev.com.sigea.apresentacao.conteudo_didatico.strategy.DeadlineOpcionalStrategy`
- `dev.com.sigea.apresentacao.conteudo_didatico.strategy.ValidacaoEscopoStrategy` (interface)
- `dev.com.sigea.apresentacao.conteudo_didatico.strategy.ValidacaoTurmaProfessorStrategy`

### 1.2. Decorator Pattern - Enriquecimento de Materiais
**Objetivo:** Adicionar funcionalidades extras aos materiais (metadados, versão, indicador de prazo) de forma dinâmica.

**Classes:**
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialEnriquecido` (interface)
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialBase`
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialDecorator` (classe abstrata)
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialComMetadata`
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialComVersao`
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialComIndicadorPrazo`
- `dev.com.sigea.apresentacao.conteudo_didatico.decorator.MaterialEnriquecido` (implementação)

### 1.3. Repository Pattern - Persistência de Turmas e Materiais
**Objetivo:** Abstrair a camada de persistência para turmas, materiais e atividades.

**Classes:**
- `dev.com.sigea.dominio.turma.TurmaRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.TurmaRepositoryImpl`
- `dev.com.sigea.dominio.turma.TurmaService`
- `dev.com.sigea.infraestrutura.service.TurmaServiceImpl`

### 1.4. DTO Pattern - Transferência de Dados
**Objetivo:** Transferir dados entre camadas sem expor entidades de domínio.

**Classes:**
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.TurmaResponse`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.MaterialResponse`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.AtividadeResponse`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.PublicarMaterialRequest`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.CriarAtividadeRequest`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.CriarTurmaRequest`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.AnexoRequest`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.AnexoResponse`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.CorrigirAtividadeRequest`
- `dev.com.sigea.apresentacao.conteudo_didatico.dto.EnvioResponse`

### 1.5. Controller Pattern (REST)
**Objetivo:** Expor endpoints REST para gerenciamento de conteúdo didático.

**Classes:**
- `dev.com.sigea.apresentacao.conteudo_didatico.ConteudoDidaticoController`

---

## 2. Gestão de Desempenho Acadêmico - Professor / Aluno

### 2.1. Template Method Pattern - Registro de Desempenho
**Objetivo:** Definir o fluxo comum para registro de notas e frequência, permitindo variações específicas.

**Classes:**
- `dev.com.sigea.apresentacao.desempenho_academico.template.RegistroDesempenhoTemplate` (classe abstrata)
- `dev.com.sigea.apresentacao.desempenho_academico.template.RegistroNotaTemplate`
- `dev.com.sigea.apresentacao.desempenho_academico.template.RegistroFrequenciaTemplate`

### 2.2. Observer Pattern - Notificações de Desempenho
**Objetivo:** Notificar observadores quando notas ou frequência são registradas.

**Classes:**
- `dev.com.sigea.apresentacao.desempenho_academico.observer.DesempenhoObserver` (interface)
- `dev.com.sigea.apresentacao.desempenho_academico.observer.DashboardAlunoObserver`
- `dev.com.sigea.apresentacao.desempenho_academico.observer.AuditoriaObserver`
- `dev.com.sigea.apresentacao.desempenho_academico.observer.EmailNotificacaoObserver`

### 2.3. Repository Pattern - Persistência de Notas e Frequência
**Objetivo:** Abstrair a persistência de dados acadêmicos.

**Classes:**
- `dev.com.sigea.dominio.sala.SalaRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.SalaRepositoryImpl`
- `dev.com.sigea.dominio.sala.LancamentoDeNotaService`
- `dev.com.sigea.infraestrutura.persistencia.AvaliacaoJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.ChamadaJpaRepository`

### 2.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.desempenho_academico.dto.LancarNotaRequest`
- `dev.com.sigea.apresentacao.desempenho_academico.dto.RegistrarFrequenciaRequest`
- `dev.com.sigea.apresentacao.desempenho_academico.dto.DesempenhoResponse`
- `dev.com.sigea.apresentacao.desempenho_academico.dto.AlunoMatriculadoResponse`

### 2.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.desempenho_academico.DesempenhoAcademicoController`
- `dev.com.sigea.apresentacao.aluno.DesempenhoAlunoController`
- `dev.com.sigea.apresentacao.notas.NotasController`
- `dev.com.sigea.apresentacao.chamada.ChamadaController`

---

## 3. Gerenciar Fóruns de Discussão – Professor / Aluno

### 3.1. Proxy Pattern - Controle de Acesso
**Objetivo:** Controlar o acesso ao fórum verificando se o usuário está matriculado na disciplina.

**Classes:**
- `dev.com.sigea.apresentacao.foruns.proxy.ForumService` (interface)
- `dev.com.sigea.apresentacao.foruns.proxy.ForumServiceReal`
- `dev.com.sigea.apresentacao.foruns.proxy.ForumServiceProxy`

### 3.2. Observer Pattern - Notificações de Fórum
**Objetivo:** Notificar professores sobre novas mensagens no fórum e marcar leituras.

**Classes:**
- `dev.com.sigea.apresentacao.foruns.observer.DashboardProfessorObserver`
- `dev.com.sigea.apresentacao.foruns.observer.MarcadorLeituraObserver`
- `dev.com.sigea.apresentacao.foruns.observer.NotificacaoForumObserver`

### 3.3. Iterator Pattern - Navegação Paginada
**Objetivo:** Iterar sobre tópicos do fórum com suporte a paginação.

**Classes:**
- `dev.com.sigea.apresentacao.foruns.iterator.TopicoPaginadoIterator`

### 3.4. Repository Pattern - Persistência de Fóruns
**Classes:**
- `dev.com.sigea.dominio.forum.TopicoRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.TopicoRepositoryImpl`
- `dev.com.sigea.dominio.forum.ForumService`
- `dev.com.sigea.infraestrutura.persistencia.TopicoJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.RespostaJpaRepository`

### 3.5. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.foruns.dto.TopicoResponse`
- `dev.com.sigea.apresentacao.foruns.dto.CriarTopicoRequest`
- `dev.com.sigea.apresentacao.foruns.dto.ResponderTopicoRequest`
- `dev.com.sigea.apresentacao.foruns.RespostaResponse`

### 3.6. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.foruns.ForunsController`

---

## 4. Gerenciar Avisos e Comunicados - Professor / Administrador

### 4.1. Decorator Pattern - Enriquecimento de Avisos
**Objetivo:** Adicionar funcionalidades extras aos avisos (prioridade, escopo, anexos, expiração) de forma dinâmica.

**Classes:**
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoEnriquecido` (interface)
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoBase`
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoDecorator` (classe abstrata)
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoComPrioridade`
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoComEscopo`
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoComAnexos`
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoComExpiracao`
- `dev.com.sigea.apresentacao.avisos.decorator.AvisoEnriquecido` (implementação)

### 4.2. Observer Pattern - Notificações de Avisos
**Objetivo:** Notificar usuários sobre novos avisos e atualizar dashboards.

**Classes:**
- `dev.com.sigea.apresentacao.avisos.observer.AvisoObserver` (interface)
- `dev.com.sigea.apresentacao.avisos.observer.DashboardAlunoObserver`
- `dev.com.sigea.apresentacao.avisos.observer.DashboardProfessorObserver`
- `dev.com.sigea.apresentacao.avisos.observer.RegistroLeituraObserver`

### 4.3. Repository Pattern - Persistência de Avisos
**Classes:**
- `dev.com.sigea.dominio.aviso.AvisoRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.AvisoRepositoryImpl`
- `dev.com.sigea.dominio.aviso.PublicadorDeAvisoService`
- `dev.com.sigea.infraestrutura.persistencia.AvisoJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.AvisoLeituraJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.AvisoTurmaJpaRepository`

### 4.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.avisos.dto.AvisoResponse`
- `dev.com.sigea.apresentacao.avisos.dto.PublicarAvisoRequest`
- `dev.com.sigea.apresentacao.avisos.dto.MarcarLidoRequest`

### 4.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.avisos.AvisosController`

---

## 5. Enviar e Acompanhar Atividades - Aluno

### 5.1. Template Method Pattern - Fluxo de Envio
**Objetivo:** Definir o fluxo comum para envio de atividades, permitindo variações (envio normal vs. reenvio).

**Classes:**
- `dev.com.sigea.apresentacao.atividades_aluno.template.EnvioAtividadeTemplate` (classe abstrata)
- `dev.com.sigea.apresentacao.atividades_aluno.template.EnvioNormalTemplate`
- `dev.com.sigea.apresentacao.atividades_aluno.template.ReenvioTemplate`

### 5.2. Strategy Pattern - Determinação de Status
**Objetivo:** Determinar o status do envio (Pendente, Enviado, Atrasado, Corrigido) baseado em diferentes estratégias.

**Classes:**
- `dev.com.sigea.apresentacao.atividades_aluno.strategy.StatusEnvioStrategy` (interface)
- `dev.com.sigea.apresentacao.atividades_aluno.strategy.StatusPendenteStrategy`
- `dev.com.sigea.apresentacao.atividades_aluno.strategy.StatusEnviadoStrategy`
- `dev.com.sigea.apresentacao.atividades_aluno.strategy.StatusCorrigidoStrategy`

### 5.3. Repository Pattern - Persistência de Envios
**Classes:**
- `dev.com.sigea.dominio.turma.TurmaRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.TurmaRepositoryImpl`
- `dev.com.sigea.infraestrutura.persistencia.EnvioAtividadeJpaRepository`

### 5.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.atividades_aluno.dto.EnviarAtividadeRequest`
- `dev.com.sigea.apresentacao.atividades_aluno.dto.EnvioResponse`
- `dev.com.sigea.apresentacao.atividades_aluno.dto.IngressarTurmaRequest`

### 5.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.atividades_aluno.AtividadesAlunoController`
- `dev.com.sigea.apresentacao.aluno.AlunoTurmasController`

---

## 6. Realizar Matrícula - Aluno

### 6.1. Proxy Pattern - Validação de Regras de Negócio
**Objetivo:** Validar período ativo, vagas disponíveis e choque de horários antes de efetivar a matrícula.

**Classes:**
- `dev.com.sigea.apresentacao.matricula.proxy.MatriculaService` (interface)
- `dev.com.sigea.apresentacao.matricula.proxy.MatriculaServiceReal`
- `dev.com.sigea.apresentacao.matricula.proxy.MatriculaServiceProxy`

### 6.2. Iterator Pattern - Navegação de Salas
**Objetivo:** Iterar sobre salas disponíveis para matrícula.

**Classes:**
- `dev.com.sigea.apresentacao.matricula.iterator.SalaIterator`

### 6.3. Repository Pattern - Persistência de Matrículas
**Classes:**
- `dev.com.sigea.dominio.sala.MatriculaService`
- `dev.com.sigea.infraestrutura.persistencia.MatriculaJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.SalaJpaRepository`

### 6.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.matricula.dto.MatriculaRequest`
- `dev.com.sigea.apresentacao.matricula.dto.MatriculaResponse`

### 6.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.aluno.AlunoMatriculaController`
- `dev.com.sigea.apresentacao.matricula.MatriculaController`

---

## 7. Cadastrar e Gerenciar Usuários - Administrador

### 7.1. Factory Pattern - Criação de Usuários
**Objetivo:** Criar usuários (professores e alunos) com senha provisória gerada automaticamente.

**Classes:**
- `dev.com.sigea.apresentacao.usuarios_admin.factory.UsuarioFactory`

### 7.2. Strategy Pattern - Filtros de Busca
**Objetivo:** Permitir diferentes estratégias de filtragem de usuários (por nome, por perfil).

**Classes:**
- `dev.com.sigea.apresentacao.usuarios_admin.strategy.FiltroUsuarioStrategy` (interface)
- `dev.com.sigea.apresentacao.usuarios_admin.strategy.FiltroPorNomeStrategy`
- `dev.com.sigea.apresentacao.usuarios_admin.strategy.FiltroPorPerfilStrategy`

### 7.3. Repository Pattern - Persistência de Usuários
**Classes:**
- `dev.com.sigea.dominio.usuario.UsuarioRepository` (interface)
- `dev.com.sigea.infraestrutura.repositorio.UsuarioRepositoryImpl`
- `dev.com.sigea.dominio.usuario.UsuarioService`
- `dev.com.sigea.infraestrutura.persistencia.UsuarioJpaRepository`

### 7.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.usuarios_admin.dto.UsuarioResponse`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.CriarProfessorRequest`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.CriarAlunoRequest`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.EditarUsuarioRequest`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.FiltroUsuarioRequest`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.PerfilUsuarioResponse`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.HistoricoMatriculasResponse`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.DesativarUsuarioRequest`
- `dev.com.sigea.apresentacao.usuarios_admin.dto.AtivarUsuarioRequest`

### 7.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.usuarios_admin.UsuariosAdminController`

---

## 8. Criar e Controlar Disciplinas Oficiais - Administrador

### 8.1. Template Method Pattern - Gestão de Períodos Letivos
**Objetivo:** Definir o fluxo comum para criação e gestão de períodos letivos.

**Classes:**
- `dev.com.sigea.apresentacao.disciplinas_periodos.template.GestãoPeriodoTemplate` (classe abstrata)
- `dev.com.sigea.apresentacao.disciplinas_periodos.template.CriacaoPeriodoTemplate`

### 8.2. Observer Pattern - Notificações de Disciplinas
**Objetivo:** Notificar sobre criação de disciplinas e períodos letivos.

**Classes:**
- `dev.com.sigea.apresentacao.disciplinas_periodos.observer.DisciplinaObserver` (interface)
- `dev.com.sigea.apresentacao.disciplinas_periodos.observer.NotificacaoDisciplinaObserver`
- `dev.com.sigea.apresentacao.disciplinas_periodos.observer.AuditoriaDisciplinaObserver`

### 8.3. Repository Pattern - Persistência de Disciplinas e Períodos
**Classes:**
- `dev.com.sigea.dominio.disciplina.DisciplinaRepository` (interface)
- `dev.com.sigea.dominio.periodoletivo.PeriodoLetivoRepository` (interface)
- `dev.com.sigea.dominio.estruturaacademica.EstruturaAcademicaService`
- `dev.com.sigea.infraestrutura.persistencia.DisciplinaJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.PeriodoLetivoJpaRepository`
- `dev.com.sigea.infraestrutura.persistencia.PeriodoJpaRepository`

### 8.4. DTO Pattern - Transferência de Dados
**Classes:**
- `dev.com.sigea.apresentacao.disciplinas_periodos.dto.DisciplinaResponse`
- `dev.com.sigea.apresentacao.disciplinas_periodos.dto.PeriodoLetivoResponse`

### 8.5. Controller Pattern (REST)
**Classes:**
- `dev.com.sigea.apresentacao.disciplinas_periodos.DisciplinasPeriodosController`
- `dev.com.sigea.apresentacao.salas.SalasController`

---

## Padrões Arquiteturais Gerais

### Repository Pattern (Aplicado Globalmente)
**Objetivo:** Abstrair a camada de persistência em todas as funcionalidades.

**Classes Principais:**
- Interfaces no domínio: `TurmaRepository`, `UsuarioRepository`, `TopicoRepository`, `AvisoRepository`, `SalaRepository`, `DisciplinaRepository`, `PeriodoLetivoRepository`
- Implementações na infraestrutura: `TurmaRepositoryImpl`, `UsuarioRepositoryImpl`, `TopicoRepositoryImpl`, `AvisoRepositoryImpl`, `SalaRepositoryImpl`
- JPA Repositories: `TurmaJpaRepository`, `UsuarioJpaRepository`, `TopicoJpaRepository`, `AvisoJpaRepository`, `SalaJpaRepository`, `MatriculaJpaRepository`, `DisciplinaJpaRepository`, `PeriodoLetivoJpaRepository`, `AvaliacaoJpaRepository`, `ChamadaJpaRepository`, `EnvioAtividadeJpaRepository`, `RespostaJpaRepository`, `AvisoLeituraJpaRepository`, `AvisoTurmaJpaRepository`, `EventoJpaRepository`, `EventoProfessorJpaRepository`

### Service Pattern (Aplicado Globalmente)
**Objetivo:** Encapsular a lógica de negócio em serviços.

**Classes Principais:**
- `dev.com.sigea.dominio.turma.TurmaService`
- `dev.com.sigea.dominio.usuario.UsuarioService`
- `dev.com.sigea.dominio.usuario.AutenticacaoService`
- `dev.com.sigea.dominio.forum.ForumService`
- `dev.com.sigea.dominio.aviso.PublicadorDeAvisoService`
- `dev.com.sigea.dominio.sala.MatriculaService`
- `dev.com.sigea.dominio.sala.LancamentoDeNotaService`
- `dev.com.sigea.dominio.estruturaacademica.EstruturaAcademicaService`
- `dev.com.sigea.infraestrutura.service.TurmaServiceImpl`

### DTO Pattern (Aplicado Globalmente)
**Objetivo:** Transferir dados entre camadas sem expor entidades de domínio.

**Localização:** Todas as classes DTO estão em pacotes `dto` dentro de cada módulo de apresentação.

### Controller Pattern - REST (Aplicado Globalmente)
**Objetivo:** Expor endpoints REST para todas as funcionalidades.

**Classes Principais:**
- `dev.com.sigea.apresentacao.autenticacao.AutenticacaoController`
- `dev.com.sigea.apresentacao.dashboard.DashboardController`
- `dev.com.sigea.apresentacao.upload.UploadController`
- `dev.com.sigea.apresentacao.professor.ProfessorController`
- `dev.com.sigea.apresentacao.professor.PerfilProfessorController`
- `dev.com.sigea.apresentacao.professor.ProfessorEventosController`
- `dev.com.sigea.apresentacao.aluno.PerfilAlunoController`
- `dev.com.sigea.apresentacao.aluno.CalendarioAlunoController`
- `dev.com.sigea.apresentacao.eventos.EventosController`

---

## Resumo dos Padrões por Categoria

### Padrões Criacionais
- **Factory Pattern**: Usado na funcionalidade 7 (Cadastrar Usuários)
- **Builder Pattern**: Implícito através de construtores e setters em DTOs

### Padrões Estruturais
- **Decorator Pattern**: Usado nas funcionalidades 1 (Materiais) e 4 (Avisos)
- **Proxy Pattern**: Usado nas funcionalidades 3 (Fóruns) e 6 (Matrículas)
- **Repository Pattern**: Usado em todas as funcionalidades

### Padrões Comportamentais
- **Strategy Pattern**: Usado nas funcionalidades 1 (Validação de Deadline), 5 (Status de Envio) e 7 (Filtros)
- **Observer Pattern**: Usado nas funcionalidades 2 (Desempenho), 3 (Fóruns), 4 (Avisos) e 8 (Disciplinas)
- **Template Method Pattern**: Usado nas funcionalidades 2 (Desempenho), 5 (Envio de Atividades) e 8 (Períodos)
- **Iterator Pattern**: Usado nas funcionalidades 3 (Fóruns) e 6 (Matrículas)

### Padrões Arquiteturais
- **Repository Pattern**: Aplicado globalmente
- **Service Pattern**: Aplicado globalmente
- **DTO Pattern**: Aplicado globalmente
- **Controller Pattern (REST)**: Aplicado globalmente
- **Layered Architecture**: Separação em camadas (domínio, aplicação, infraestrutura, apresentação)

