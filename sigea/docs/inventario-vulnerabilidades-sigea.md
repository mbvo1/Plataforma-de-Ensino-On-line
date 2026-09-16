# Inventário de Vulnerabilidades — SIGEA

**Projeto:** SecureAI Lab — Cibersegurança Aplicada a Dados e IA
**Aplicação auditada:** SIGEA — Plataforma de Ensino On-line
**Stack:** Java 17, Spring Boot, H2, Flyway, frontend HTML/JS estático
**Método:** análise estática de código (SAST manual)
**Escopo:** 245 arquivos Java, 40 páginas HTML, 24 migrations

> **Ressalva metodológica:** este inventário foi produzido por leitura de
> código, sem execução da aplicação. Cada item marcado como *não confirmado
> em runtime* precisa ser validado dinamicamente antes de entrar no relatório
> como evidência. A validação faz parte da etapa de exploração.

> **Nota sobre versionamento OWASP:** as referências abaixo seguem a
> nomenclatura do OWASP Top 10. Confirme no site oficial da OWASP qual é a
> edição vigente antes de citar versão no relatório final.

---

## Sumário executivo

| Severidade | Quantidade |
|---|---|
| Crítica | 4 |
| Alta | 4 |
| Média | 4 |
| Baixa | 1 |
| **Total** | **13** |

Categorias do PDF da disciplina cobertas: falhas de autenticação, falhas de
autorização, controle de acesso inadequado, exposição indevida de dados,
falhas de configuração, gerenciamento de sessão, falhas em APIs, upload
inseguro, ausência de criptografia, falhas de integridade.

Categorias **não** cobertas pelo código atual: injeção SQL, XSS armazenado,
vulnerabilidades de IA. Ver seção "Lacunas" ao final.

---

## V-01 — Senhas armazenadas em texto claro

- **Severidade:** Crítica
- **Categoria (PDF):** Ausência ou utilização inadequada de criptografia
- **OWASP:** Falhas criptográficas
- **CWE:** CWE-256 (armazenamento de credencial sem proteção), CWE-916

**Localização**

`dominio-principal/src/main/java/dev/com/sigea/dominio/usuario/AutenticacaoService.java`, linhas 42–44:

```java
private String hashSenha(String senhaTexto) {
    return "HASH_" + senhaTexto;
}
```

**Descrição**

A função nomeada `hashSenha` não aplica nenhuma função de hash. Ela concatena
o prefixo literal `HASH_` à senha em texto claro e persiste o resultado na
coluna `senha_hash`. O nome do método e da coluna sugerem proteção que não
existe. A comparação em `Senha.verificar()` é um `equals()` de string, o que
adicionalmente expõe a comparação a ataque de temporização.

**Método de exploração**

1. Acessar o console H2 em `/h2-console` (ver V-04)
2. Executar `SELECT email, senha_hash FROM Usuarios`
3. Remover o prefixo `HASH_` de cada valor — a senha está em claro

Confirmado estaticamente nas migrations: `V2__inserir_admin_padrao.sql` grava
`HASH_admin123`, e `V12__corrigir_senhas_professores.sql` aplica
`HASH_senha123` a todos os professores.

**Evidência a capturar**

Screenshot da tabela `Usuarios` no console H2 com a coluna `senha_hash` legível.

**Impacto**

Comprometimento total de todas as contas. Como usuários reutilizam senhas
entre serviços, o impacto extrapola a aplicação. Viola o Art. 46 da LGPD
(medidas de segurança para dados pessoais).

**Correção**

Substituir por Argon2id ou BCrypt, com salt por usuário e fator de custo
calibrado. Verificar na documentação vigente do Spring Security a API atual de
`PasswordEncoder` — não assuma assinaturas de memória. Migration adicional
para reidratar as senhas existentes (ou forçar redefinição).

---

## V-02 — Ausência total de autenticação na camada de API

- **Severidade:** Crítica
- **Categoria (PDF):** Falhas de autenticação / Falhas em APIs
- **OWASP:** Controle de acesso quebrado; API — autenticação quebrada
- **CWE:** CWE-306 (função crítica sem autenticação)

**Localização**

Toda a camada `apresentacao-backend`. Confirmado por ausência:

- `spring-boot-starter-security` não consta em `apresentacao-backend/pom.xml`
- nenhuma ocorrência de `HttpSession`, `Cookie` ou `jwt` em todo o código Java
- nenhum filtro, interceptor ou anotação de segurança nos 22 controllers

**Descrição**

`AutenticacaoController.login()` valida credenciais e devolve um
`AuthResponse` com id, nome, e-mail e perfil — mas **não emite token nem
cria sessão**. Não existe estado de autenticação no servidor. O "login" é
puramente decorativo: o frontend guarda `usuarioNome` no `localStorage` e
segue navegando. Todos os endpoints respondem a qualquer requisição não
autenticada.

**Método de exploração**

```bash
curl http://localhost:8080/api/aluno/1/desempenho
curl http://localhost:8080/api/usuarios
```

Sem cabeçalho de autorização, sem cookie, sem login prévio.

**Evidência a capturar**

Terminal com a resposta JSON completa de um endpoint sensível, em sessão
limpa (aba anônima / cliente HTTP separado).

**Impacto**

Toda a base de dados é legível e alterável por qualquer pessoa com acesso de
rede à aplicação. É a vulnerabilidade raiz da qual V-03 e V-06 derivam.

**Correção**

Spring Security com autenticação stateless por JWT (ou sessão server-side, se
a equipe preferir justificar essa escolha). Filtro aplicado a `/api/**` com
allowlist explícita para os endpoints públicos de login e registro.

---

## V-03 — IDOR generalizado nos endpoints de aluno

- **Severidade:** Crítica
- **Categoria (PDF):** Falhas de autorização / Controle de acesso inadequado
- **OWASP:** Controle de acesso quebrado; API — autorização quebrada em nível de objeto
- **CWE:** CWE-639 (autorização contornada por chave controlada pelo usuário)

**Localização**

Padrão recorrente em múltiplos controllers. Exemplos confirmados:

```
GET    /api/aluno/{alunoId}/desempenho          DesempenhoAlunoController
GET    /api/aluno/{alunoId}/resumo
GET    /api/alunos/{id}/historico
GET    /api/atividades/{atividadeId}/envios
DELETE /api/{alunoId}/matriculas/{matriculaId}
DELETE /api/{alunoId}/atividades/{atividadeId}/envio
```

**Descrição**

Em `DesempenhoAlunoController.buscarDesempenho()`, o identificador vem do path
(`@PathVariable Long alunoId`) e a única verificação é de **existência**
(`usuarioRepository.findById(alunoId)`), nunca de **propriedade**. Não há
comparação entre o `alunoId` solicitado e a identidade do requisitante —
identidade que, por V-02, sequer existe.

Os endpoints `DELETE` agravam o quadro: permitem apagar matrícula e entrega de
atividade de terceiros.

**Método de exploração**

Autenticar como aluno A (id 3), depois requisitar `/api/aluno/4/desempenho`.
Retorna notas, faltas e histórico do aluno B. Incremento sequencial do id
permite enumerar toda a base.

**Evidência a capturar**

Duas requisições lado a lado: id próprio e id alheio, ambas retornando 200.

**Impacto**

Vazamento de desempenho acadêmico de toda a instituição — dado pessoal sob a
LGPD. Nos endpoints DELETE, perda de integridade e disponibilidade de dados
de terceiros.

**Correção**

Derivar a identidade do token, não do path. Validar propriedade em cada
operação, com falha segura (negar por padrão). Aplicar o princípio do menor
privilégio: professor acessa apenas alunos de suas turmas; admin acessa todos,
com registro em log.

---

## V-04 — Console de banco de dados exposto sem autenticação

- **Severidade:** Crítica
- **Categoria (PDF):** Falhas de configuração
- **OWASP:** Configuração incorreta de segurança
- **CWE:** CWE-1188 (inicialização com configuração insegura)

**Localização**

`apresentacao-backend/src/main/resources/application.properties`:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.username=sa
spring.datasource.password=
```

**Descrição**

Console web do H2 habilitado, em rota previsível, com usuário `sa` e senha em
branco. Sem Spring Security (V-02), a rota não tem qualquer proteção. O
`README.md` do repositório documenta publicamente a URL JDBC e informa que a
senha deve ser deixada em branco.

**Método de exploração**

Navegar até `/h2-console`, preencher `jdbc:h2:file:./sigea`, usuário `sa`,
senha vazia. Acesso SQL irrestrito: leitura, escrita e DDL.

**Evidência a capturar**

Screenshot do console conectado, executando `SELECT * FROM Usuarios`.

**Impacto**

Comprometimento total da confidencialidade, integridade e disponibilidade dos
dados. Encadeado com V-01, entrega todas as credenciais em claro.

**Correção**

`spring.h2.console.enabled=false` em qualquer perfil que não seja
desenvolvimento local. Migrar para perfis Spring separados
(`application-dev.properties` / `application-prod.properties`). Senha de banco
via variável de ambiente.

---

## V-05 — Upload de arquivos sem qualquer validação

- **Severidade:** Alta
- **Categoria (PDF):** Upload inseguro de arquivos
- **OWASP:** Design inseguro / Falhas de integridade
- **CWE:** CWE-434 (upload irrestrito de arquivo de tipo perigoso)

**Localização**

`apresentacao-backend/src/main/java/dev/com/sigea/apresentacao/upload/UploadController.java`

**Descrição**

O método `uploadFile()` verifica apenas se o arquivo está vazio. Não valida:

- extensão (allowlist)
- tipo MIME declarado
- magic bytes do conteúdo real
- tamanho máximo
- autenticação do remetente

A extensão original é preservada e concatenada ao UUID gerado
(`UUID.randomUUID() + extension`), então o atacante controla parte do nome
final do arquivo.

**Método de exploração**

```bash
curl -F "file=@shell.jsp" http://localhost:8080/api/upload
```

Sem login. Testar também bomba de descompressão e arquivo de vários GB para
exaurir disco.

> *Não confirmado em runtime:* se o diretório `uploads/` é servido
> estaticamente, um arquivo enviado poderia ser executado ou baixado
> diretamente. É preciso testar essa hipótese antes de afirmá-la no relatório.
> Se não for servido, a severidade cai para Média.

**Evidência a capturar**

Upload bem-sucedido de arquivo não-PDF, seguido de tentativa de acesso direto
ao caminho retornado pela API.

**Impacto**

Na melhor hipótese, poluição de armazenamento e negação de serviço por
esgotamento de disco. Na pior, execução remota de código.

**Correção**

Allowlist de extensões, validação por magic bytes (não confiar no
`Content-Type` enviado), limite de tamanho via
`spring.servlet.multipart.max-file-size`, nome totalmente aleatório sem
extensão herdada, armazenamento fora do diretório servido, e exigência de
autenticação.

---

## V-06 — Ausência de controle de acesso baseado em papel

- **Severidade:** Alta
- **Categoria (PDF):** Controle de acesso inadequado
- **OWASP:** Controle de acesso quebrado; API — autorização quebrada em nível de função
- **CWE:** CWE-862 (autorização ausente)

**Localização**

Todos os controllers administrativos, entre eles `UsuariosAdminController`,
`DisciplinasPeriodosController`, `NotasController`.

**Descrição**

O enum `Perfil` define ADMINISTRADOR, PROFESSOR e ALUNO, e o modelo de domínio
conhece esses papéis. Porém nenhum controller verifica o papel do requisitante
antes de executar a operação. Não há `@PreAuthorize`, nem verificação manual
equivalente. A separação de privilégios existe apenas como rótulo no banco e
como lógica de exibição no frontend.

**Método de exploração**

Autenticar como ALUNO e chamar diretamente um endpoint administrativo, por
exemplo o lançamento de notas ou a criação de usuário. A operação é aceita.

**Evidência a capturar**

Requisição a endpoint de admin partindo de conta de aluno, com resposta 200 e
efeito persistido no banco.

**Impacto**

Escalação vertical de privilégio. Um aluno pode alterar as próprias notas,
criar contas ou desativar usuários.

**Correção**

RBAC no servidor, com `@PreAuthorize` por papel ou filtro equivalente.
Controle no frontend é usabilidade, nunca segurança — vale registrar essa
distinção no relatório, porque é o erro conceitual central deste projeto.

---

## V-07 — Dados pessoais sem proteção criptográfica em repouso

> **Status:** corrigida na branch `fix/V-07-cifragem-cpf`. Validação
> dinâmica completa (CPF em claro confirmado + fix + round-trip cifrado
> reteste) em
> [`evidencias/v07/exploracao-V-07-cifragem-cpf.md`](evidencias/v07/exploracao-V-07-cifragem-cpf.md).
> CPF passa a ser cifrado com AES-256-GCM (nonce determinístico, para não
> quebrar a busca por CPF nem a constraint UNIQUE). Linhas gravadas antes
> do fix continuam legíveis em texto claro até serem salvas de novo (sem
> migração em lote nesta correção). E-mail permanece em claro, como o
> próprio inventário já previa como opção aceitável.

- **Severidade:** Alta
- **Categoria (PDF):** Exposição indevida de dados / Ausência de criptografia
- **OWASP:** Falhas criptográficas
- **CWE:** CWE-311 (ausência de cifragem de dado sensível)

**Localização**

`infraestrutura/src/main/resources/db/migration/V1__criar_schema_completo.sql`:

```sql
CREATE TABLE Usuarios (
    usuario_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    senha_hash VARCHAR(255),
    ...
);
```

**Descrição**

CPF e e-mail são persistidos em texto claro. O banco H2 é um arquivo local
(`jdbc:h2:file:./sigea`) sem cifragem de arquivo. O CPF é dado pessoal sob a
LGPD; sua exposição permite identificação direta do titular.

**Impacto**

Qualquer acesso ao arquivo do banco — backup, cópia do diretório, console
H2 — entrega CPFs completos. Base para fraude de identidade.

**Correção**

Cifra simétrica (AES-GCM) em nível de campo para CPF, com chave em variável
de ambiente, separada da chave de assinatura. E-mail pode permanecer em claro
se justificado por necessidade de busca, mas a decisão precisa estar
documentada. Mascaramento na exibição (`123.***.***-45`).

---

## V-08 — CORS permissivo em toda a API

- **Severidade:** Média
- **Categoria (PDF):** Falhas de configuração / Falhas em APIs
- **OWASP:** Configuração incorreta de segurança
- **CWE:** CWE-942 (política de origem cruzada excessivamente permissiva)

**Localização**

`@CrossOrigin(origins = "*")` em 15 controllers, incluindo
`AutenticacaoController` (linha 21), `DesempenhoAlunoController` (linha 15),
`SalasController`, `ChamadaController`, `AlunoMatriculaController`.

**Descrição**

Qualquer origem pode fazer requisições à API a partir do navegador da vítima.
Combinado com V-02, significa que um site malicioso pode ler dados da API em
nome de quem o visitar.

**Impacto**

Vazamento de dados via site de terceiros. Sem autenticação por cookie, o vetor
é mais limitado do que pareceria — vale explicar essa nuance no relatório, em
vez de exagerar a severidade.

**Correção**

Allowlist explícita de origens, configurada centralmente e não por anotação
espalhada em cada controller.

---

## V-09 — Exposição de dados pessoais em logs de aplicação

- **Severidade:** Média
- **Categoria (PDF):** Exposição indevida de dados
- **OWASP:** Falhas de log e monitoramento
- **CWE:** CWE-532 (inserção de informação sensível em log)

**Localização**

`AutenticacaoController.registro()`:

```java
System.out.println("DEBUG - Request recebido: nome=" + request.getNome() +
                 ", email=" + request.getEmail() +
                 ", cpf=" + request.getCpf() + ...);
```

Somado a `spring.jpa.show-sql=true` em `application.properties`, que despeja
todas as queries — com seus parâmetros — na saída padrão.

**Descrição**

Nome, e-mail e CPF de todo usuário registrado vão para o log em texto claro.
A senha está mascarada nesse ponto específico, o que mostra que houve
preocupação parcial — mas incompleta.

**Impacto**

Dados pessoais persistidos em local sem controle de acesso, retenção ou
expurgo. Viola o princípio de minimização da LGPD.

**Correção**

Remover o `println`. Adotar logging estruturado (SLF4J) com níveis
apropriados, mascaramento de campos sensíveis e `show-sql=false` fora de
desenvolvimento.

---

## V-10 — Credenciais padrão fixas em código e documentação

- **Severidade:** Média
- **Categoria (PDF):** Falhas de autenticação / Falhas de configuração
- **OWASP:** Configuração incorreta de segurança
- **CWE:** CWE-798 (credencial embutida em código)

**Localização**

- `README.md`: publica e-mail, CPF e senha do administrador
- `V2__inserir_admin_padrao.sql`: `admin@sigea.com` / `admin123`
- `V12__corrigir_senhas_professores.sql`: `senha123` para **todos** os professores

**Descrição**

Contas privilegiadas criadas com senha previsível, sem obrigatoriedade de
troca no primeiro acesso. A migration V12 uniformiza a senha de todos os
professores, criando um único ponto de falha para o perfil inteiro.

**Impacto**

Acesso administrativo trivial a partir de informação pública do repositório.

**Correção**

Senha inicial aleatória gerada na primeira execução, flag de troca obrigatória
no primeiro login, e remoção das credenciais do README.

---

## V-11 — Ausência de registro de eventos de segurança

- **Severidade:** Média
- **Categoria (PDF):** Falhas de configuração / requisito obrigatório nº 12
- **OWASP:** Falhas de log e monitoramento
- **CWE:** CWE-778 (log insuficiente)

**Descrição**

Não existe trilha de auditoria. Tentativas de login falhas, mudanças de
privilégio, lançamentos de nota, exclusões e uploads não são registrados.
Os únicos "logs" são `System.out.println` de depuração (V-09). Não há
detecção, nem rastreabilidade, nem capacidade de resposta a incidente.

**Impacto**

Um comprometimento ocorrido via qualquer das vulnerabilidades acima seria
indetectável e não reconstituível.

**Correção**

Tabela de auditoria com encadeamento por hash (cada registro armazena o hash
do anterior), o que cobre simultaneamente o requisito de logs e o de
integridade. Ver seção de recomendações no plano de adequação.

---

## V-12 — `flyway.clean-disabled=false`

- **Severidade:** Baixa
- **Categoria (PDF):** Falhas de configuração
- **CWE:** CWE-16

**Localização**

`application.properties`: `spring.flyway.clean-disabled=false`

**Descrição**

Habilita o comando `flyway:clean`, que apaga todos os objetos do schema. Não é
explorável remotamente pela aplicação — a exposição é operacional, não de
rede. Incluído no inventário por completude e por ser trivial de corrigir.

**Impacto**

Perda total de dados por erro operacional. Afeta disponibilidade.

**Correção**

`spring.flyway.clean-disabled=true`.

---

## V-13 — XSS armazenado via nome do aluno (achado em segunda revisão)

- **Severidade:** Alta
- **Categoria (PDF):** XSS
- **OWASP:** Injeção / Falhas de integridade de software e dados
- **CWE:** CWE-79 (neutralização inadequada de entrada durante geração de página web)

**Localização**

`apresentacao-frontend/public/atividade-detalhes.html`, linha 631:

```javascript
return `
    <tr>
        <td>
            <strong>${aluno.nomeAluno}</strong>
            ...
```

**Descrição**

A tela que o professor usa para corrigir entregas monta a tabela de alunos via
`innerHTML`, inserindo `aluno.nomeAluno` diretamente na string, sem qualquer
escape. O campo `nome` é definido pelo próprio usuário no registro
(`AutenticacaoController`), sem sanitização na entrada nem na saída.

Esta é uma correção em relação à análise anterior deste inventário, que havia
classificado esta ocorrência de `innerHTML` como "não confirmada em runtime".
A leitura completa do trecho confirma que o dado do usuário entra sem escape.

**Método de exploração**

1. Registrar um aluno com nome `<img src=x onerror="alert(document.cookie)">`
2. Fazer esse aluno enviar uma atividade
3. Um professor abre `atividade-detalhes.html` para corrigir a turma
4. O script executa no navegador do professor

**Evidência a capturar**

Screenshot do alerta disparado na tela do professor, mais o payload usado no
cadastro.

**Impacto**

Execução de script arbitrário no contexto do professor — que, nesta
aplicação, é o perfil com mais privilégio de leitura sobre dados de turma.
Pode ser usado para roubo de sessão (uma vez implementada, na Frente 1) ou
para forjar ações em nome do professor.

**Correção**

Nunca montar HTML por concatenação de string com dado de usuário. Usar
`textContent` para texto puro, ou uma função de escape antes de interpolar em
HTML. Nunca aceitar dado do usuário sem validação, também na entrada (registro
do nome).

---

## Achado fora do escopo técnico: dados reais versionados

Não é vulnerabilidade de código, mas exige ação imediata.

O diretório `apresentacao-backend/uploads/` está versionado no Git com oito
PDFs reais, entre eles `1765596374958_Boleto227880.pdf` e
`1765596374958_Projeto_Final_TEORIADOSGRAFOS.pdf`. Os arquivos não foram
abertos durante esta análise.

Se houver dado pessoal real nesses arquivos, trata-se de tratamento
inadequado sob a LGPD, agravado por estar em repositório versionado. Ações:
remover os arquivos, adicionar `uploads/` ao `.gitignore`, repovoar com PDFs
fictícios. Considerar reescrita de histórico se o repositório for público.

Aproveitar no relatório como caso concreto de violação do princípio de
minimização — dado real em ambiente de desenvolvimento é falha clássica e
rende boa análise crítica.

---

## Lacunas: categorias do PDF ainda não cobertas

O código atual **não** apresenta estas três categorias. Para cobri-las, será
necessário introduzi-las deliberadamente e registrar no relatório que a
inserção foi intencional e controlada.

| Categoria | Situação | Como cobrir |
|---|---|---|
| Injeção SQL | Ausente — o projeto usa Spring Data JPA, que parametriza por padrão | Introduzir um endpoint de busca com query concatenada, em branch identificada |
| XSS armazenado | **Confirmado — ver V-13** | Já existe, ataque direto, sem necessidade de construir nada |
| Vulnerabilidades de IA | Ausente — não há funcionalidade de IA | Surge junto com a implementação da IA (prompt injection via arquivo enviado) |

---

## Matriz consolidada

| ID | Vulnerabilidade | Categoria PDF | Severidade | Requisito que destrava |
|---|---|---|---|---|
| V-01 | Senha em texto claro | Criptografia | Crítica | 9, 10 |
| V-02 | API sem autenticação | Autenticação / API | Crítica | 1 |
| V-03 | IDOR em endpoints de aluno | Autorização | Crítica | 8 |
| V-04 | Console H2 exposto | Configuração | Crítica | — |
| V-05 | Upload sem validação | Upload inseguro | Alta | 6 |
| V-06 | Sem RBAC | Controle de acesso | Alta | 2, 8 |
| V-07 | CPF sem cifragem | Exposição / Criptografia | Alta | 9 |
| V-08 | CORS permissivo | Configuração / API | Média | — |
| V-09 | Dados pessoais em log | Exposição | Média | 12 |
| V-10 | Credenciais padrão | Autenticação | Média | 1 |
| V-11 | Sem log de segurança | Monitoramento | Média | 12 |
| V-12 | Flyway clean habilitado | Configuração | Baixa | — |
| V-13 | XSS armazenado (nome do aluno) | XSS | Alta | — |

---

## Próximos passos

1. Executar a aplicação e validar dinamicamente cada item, capturando
   evidência (screenshot, saída de terminal, requisição HTTP)
2. Reclassificar severidades conforme o resultado da validação
3. Converter este inventário em matriz de riscos, acrescentando probabilidade
   e impacto por ativo
4. Priorizar correções: V-02 e V-06 primeiro, pois destravam a correção de V-03
5. Registrar a v1 vulnerável em tag Git antes de qualquer correção, para que o
   diff sirva de evidência de antes/depois
