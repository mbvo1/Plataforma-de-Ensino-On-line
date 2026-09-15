# Guia de Exploração — SecureAI Lab / SIGEA

**Objetivo desta etapa:** atacar cada vulnerabilidade na versão atual (ainda
sem correções) e capturar evidência de que o ataque funciona. Isso vem
**antes** de qualquer correção — sem essa evidência, perde-se a prova do
"antes" exigida no relatório.

## Preparação (fazer uma vez, todo o grupo)

1. Confirme que está na branch `main`, na tag `v1-vulneravel`:
   ```powershell
   git checkout main
   git log --oneline -1
   ```
2. Suba a aplicação:
   ```powershell
   cd sigea/apresentacao-backend
   mvn spring-boot:run
   ```
3. Tenha à mão: navegador, um cliente HTTP (Postman ou `curl.exe`), e o
   console H2 em `http://localhost:8080/h2-console`
   (JDBC URL `jdbc:h2:file:./sigea`, user `sa`, senha em branco).
4. Login de admin já existente: `admin@sigea.com` / `admin123`.
5. Para cada vulnerabilidade abaixo, capture: **screenshot** ou **saída de
   terminal** mostrando o ataque funcionando, e salve com o nome
   `V-0X-antes.png` (ou `.txt`) numa pasta `evidencias/` no repositório.

---

## As 14 vulnerabilidades, todas aprofundadas

### V-02 — API sem autenticação
**Objetivo:** provar que qualquer endpoint responde sem login.
```powershell
curl.exe http://localhost:8080/api/aluno/1/desempenho
```
Sem cabeçalho de autorização, sem cookie. **Evidência:** resposta 200 com
dados completos do aluno, capturada de uma aba anônima/terminal limpo.

### V-03 — IDOR nos endpoints de aluno
**Objetivo:** acessar dado de um aluno logado como outro.
1. Descubra dois `usuario_id` de alunos diferentes via H2 console:
   `SELECT usuario_id, nome FROM Usuarios WHERE perfil='ALUNO';`
2. Chame o endpoint trocando o id:
   ```powershell
   curl.exe http://localhost:8080/api/aluno/<id-do-outro-aluno>/desempenho
   ```
**Evidência:** duas capturas lado a lado — um id, depois o outro — ambos
retornando dado completo.

### V-01 — Senha em texto claro
**Objetivo:** ler senha de um usuário direto no banco.
No console H2:
```sql
SELECT nome, email, senha_hash FROM Usuarios;
```
**Evidência:** print da coluna `senha_hash` mostrando `HASH_admin123` (ou
outra) — deixe claro na legenda que remover o prefixo `HASH_` revela a senha
real.

### V-04 — Console H2 exposto
**Objetivo:** provar acesso irrestrito ao banco pela web.
Acesse `http://localhost:8080/h2-console` sem estar logado em lugar nenhum
da aplicação, conecte com `sa` / senha em branco.
**Evidência:** print da tela conectada, rodando `SELECT * FROM Usuarios;`.

### V-13 — XSS armazenado (nome do aluno)
**Objetivo:** executar script no navegador do professor.
1. Registre um aluno novo em `http://localhost:8080`, usando como nome:
   ```
   <img src=x onerror="alert('XSS - '+document.cookie)">
   ```
2. Faça esse aluno se matricular numa turma e enviar uma atividade
   (ou insira via H2 console, como fizemos para o teste da IA).
3. Logue como professor e abra a tela de correção da atividade
   (`atividade-detalhes.html`) daquela turma.
**Evidência:** screenshot do alerta disparando na tela do professor.

### V-05 — Upload sem validação
**Objetivo:** enviar um arquivo que não é PDF.
```powershell
curl.exe -F "file=@qualquer_arquivo.exe" http://localhost:8080/api/upload
```
(pode ser qualquer arquivo `.exe`, `.txt` ou `.bat` que você tenha à mão)
**Evidência:** resposta 200 aceitando o upload, sem checagem de tipo.

### V-11 — Ausência de log de segurança
**Objetivo:** provar que nenhum evento sensível fica registrado.
1. Tente logar com senha errada várias vezes.
2. Vá ao terminal do servidor e confirme: não há nenhuma linha de log
   relacionada à tentativa de login falha — só as queries SQL do Hibernate.
**Evidência:** print do terminal mostrando a ausência (comente na legenda
que a única saída visível são os `Hibernate:` de depuração, não eventos de
segurança).

### IA — Prompt injection
**Objetivo:** manipular a nota via instrução escondida no PDF.
*(Farei o PDF malicioso e o script de inserção quando você pedir — é o
próximo passo depois deste guia.)*
**Evidência:** JSON de resposta do endpoint mostrando nota 10 atribuída a um
conteúdo vazio/nonsense.

---

## Grupo 2 — As 6 restantes, agora também aprofundadas

### V-06 — Sem controle de acesso por papel (RBAC)
**Objetivo:** provar que uma conta ALUNO consegue executar uma ação
reservada a ADMIN/PROFESSOR.
1. Registre ou use um aluno já existente.
2. Chame diretamente um endpoint administrativo, por exemplo a criação de
   usuário ou o lançamento de nota, sem passar pela tela de admin:
   ```powershell
   curl.exe -X POST http://localhost:8080/api/usuarios -H "Content-Type: application/json" -d "{\"nome\":\"Invasor\",\"email\":\"invasor@teste.com\",\"cpf\":\"99999999999\",\"senha\":\"123456\",\"perfil\":\"ADMINISTRADOR\"}"
   ```
   (ajuste o corpo conforme o endpoint real de criação de usuário que vocês
   encontrarem no `UsuariosAdminController`)
**Evidência:** resposta 200/201 confirmando que a conta foi criada com
perfil ADMINISTRADOR, mesmo sem nenhuma verificação de quem fez a chamada.

### V-07 — CPF sem cifragem
**Objetivo:** ler o CPF de qualquer usuário em texto claro.
No console H2:
```sql
SELECT nome, cpf FROM Usuarios;
```
**Evidência:** print da coluna `cpf` com os números completos e legíveis,
sem nenhuma máscara ou cifra.

### V-08 — CORS permissivo
**Objetivo:** provar que qualquer origem pode chamar a API.
1. Abra o DevTools do navegador (F12) → aba "Network"
2. Faça login normalmente e observe qualquer chamada à API
3. Clique na requisição → aba "Headers" → procure a resposta
**Evidência:** print do cabeçalho de resposta mostrando
`Access-Control-Allow-Origin: *`. Alternativa mais didática: crie um HTML
simples fora do projeto (`teste-cors.html`, aberto direto no navegador, fora
do `localhost:8080`) com um `fetch()` para a API do SIGEA, e mostre que ele
recebe a resposta mesmo vindo de outra origem.

### V-09 — Dados pessoais em log
**Objetivo:** capturar CPF e e-mail vazando no console do servidor.
1. Registre um novo usuário pela tela normal (`http://localhost:8080` →
   "Cadastre-se")
2. Olhe imediatamente o terminal onde o `spring-boot:run` está rodando
**Evidência:** print da linha `DEBUG - Request recebido: nome=..., email=...,
cpf=...` aparecendo no console, com o dado do usuário que você acabou de
cadastrar.

### V-10 — Credenciais padrão fixas
**Objetivo:** mostrar que contas privilegiadas nascem com senha previsível
e pública.
1. Abra o `README.md` do repositório e localize a seção com a credencial do
   admin
2. Abra a migration `V12__corrigir_senhas_professores.sql` e mostre que
   todos os professores recebem a mesma senha (`senha123`)
**Evidência:** dois prints — um do README, outro da migration. Complementar
(opcional): logue de fato com essas credenciais para provar que ainda
funcionam.

### V-12 — Flyway clean habilitado
**Objetivo:** mostrar a configuração perigosa, sem executá-la de verdade.
Abra `application.properties` e localize:
```properties
spring.flyway.clean-disabled=false
```
**Evidência:** print dessa linha, com uma nota explicando o risco: se
alguém rodar `mvn flyway:clean` ou uma ferramenta administrativa disparar
esse comando, todo o schema é apagado. **Não executem esse comando de
verdade** — isso apagaria os dados de todo o grupo. A evidência aqui é de
configuração, não de exploração ao vivo.

---

## Depois de capturar tudo isso

1. Organize as evidências na pasta `evidencias/` do repositório
2. Faça commit: `git add evidencias/ && git commit -m "Evidencias do ataque - v1 vulneravel"`
3. Só então cada dupla começa a corrigir a própria parte (ver mapeamento das
   5 frentes já combinado)
4. Depois da correção, repita o mesmo ataque em cada uma e capture a
   evidência de bloqueio — mesmo nome de arquivo, sufixo `-depois` em vez de
   `-antes`
