# Roteiro de Demonstração — Vulnerabilidades no SIGEA (pelo front-end)

Este roteiro permite **demonstrar ao vivo, pelo navegador**, quatro
vulnerabilidades da aplicação e explicá-las durante a apresentação:

| # | Vulnerabilidade | Severidade | Onde se demonstra |
|---|---|---|---|
| 1 | **IA — Prompt Injection** no feedback automático | Crítica (IA) | Tela do professor |
| 2 | **V-02 — API sem autenticação** | Crítica | Navegador / barra de endereço |
| 3 | **V-03 — IDOR** (acesso a dados de outro aluno) | Crítica | DevTools / barra de endereço |
| 4 | **V-01 — Senhas em texto claro** | Crítica | Console H2 (navegador) |

> Esta é a **versão propositalmente vulnerável** (branch `demo-apresentacao`,
> a partir da tag `v1-vulneravel`). A versão corrigida está em `main`. A ideia
> é mostrar o ataque funcionando aqui e explicar a correção que já existe em
> `main`.

---

## 0. Preparação (uma vez, antes de apresentar)

```bash
# 1. Estar na branch da demonstração
git checkout demo-apresentacao

# 2. Definir a chave da IA (necessária para a demo de IA).
#    Já existe um arquivo apresentacao-backend/.env com GEMINI_API_KEY.
#    O Spring NÃO lê o .env sozinho — exporte a variável no MESMO terminal
#    em que for subir a aplicação:
#
#    Windows PowerShell (a partir de sigea/apresentacao-backend):
#      Get-Content .env | ForEach-Object { if ($_ -match '^\s*([^#=]+)=(.*)$') { $env:($matches[1].Trim()) = $matches[2].Trim() } }
#    Git Bash / Linux:
#      export $(grep -v '^#' apresentacao-backend/.env | xargs)
#
#    Ou, simplesmente:  export GEMINI_API_KEY="SUA_CHAVE"
#
#    Sem a chave, a aplicação sobe normalmente e as vulns 2, 3 e 4 funcionam;
#    apenas a demo de IA retornará erro ao clicar em "Gerar Feedback IA".

# 3. Compilar
cd sigea
mvn clean install

# 4. (opcional) começar com banco limpo
rm -f apresentacao-backend/sigea.mv.db

# 5. Subir a aplicação em http://localhost:8080
cd apresentacao-backend
mvn spring-boot:run
#   (ou: java -jar target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar)

# 6. Em outro terminal, popular os dados da demonstração
cd sigea/demo-seguranca
bash seed-demo.sh
```

O `seed-demo.sh` cria, **sem enviar nenhum login** (isso já é a V-02):

| Papel | Login | Senha | id |
|---|---|---|---|
| Admin | admin@sigea.com | admin123 | 1 |
| Professor (Ada) | ada@sigea.com | senha123 | 2 |
| Aluna A (Alice) | alice@sigea.com | alice123 | 3 |
| Aluno B (Bruno) | bruno@sigea.com | bruno123 | 4 |

- Turma **DEMO01**, atividade "Trabalho Final - Grafos".
- **Alice já enviou o PDF com o ataque de prompt injection** (`payloads/injection-comite-pedagogico.pdf`).

> Se algo sair errado no meio da apresentação, pare a app, rode
> `rm -f apresentacao-backend/sigea.mv.db`, suba de novo e rode o seed
> novamente. Volta ao estado inicial em ~30s.

---

## 1. IA — Prompt Injection no feedback automático (Crítica)

**Ideia:** o aluno esconde no próprio trabalho um texto que se passa por uma
"pré-avaliação do comitê pedagógico". A IA obedece e atribui **nota 10** a um
trabalho vazio — e, nesta versão, a nota é **aplicada automaticamente**, sem
revisão humana.

**Passos (tela do professor):**
1. Acesse `http://localhost:8080/login-professor.html` e entre como
   **ada@sigea.com / senha123**.
2. Vá em **Turmas → Teoria dos Grafos - Demo → atividade "Trabalho Final -
   Grafos"**.
3. Abra o modal **"Ver envios"**. Você verá o envio da **Alice**.
4. Clique em **"Gerar Feedback IA"** na linha da Alice.
5. A IA responde: mostra uma **nota alta (10)** com uma justificativa
   elaborada, e a nota é preenchida/persistida automaticamente.

**O que dizer:**
- O prompt enviado ao modelo concatena instrução do sistema + conteúdo do
  aluno **sem nenhuma separação**. O modelo não distingue "o que é ordem" de
  "o que é dado do aluno".
- Um comando explícito ("ignore as instruções") tende a falhar; disfarçado
  como *contexto legítimo*, o ataque passa (ver `payloads/`:
  `ataque-explicito-bloqueado.pdf` falha, `injection-comite-pedagogico.pdf`
  tem sucesso).
- **Correção (em `main`):** delimitação estrutural entre instrução e conteúdo,
  detecção de manipulação, e **revisão humana obrigatória** — a IA só *sugere*,
  quem grava a nota é o professor.

---

## 2. V-02 — API sem autenticação (Crítica)

**Ideia:** não existe autenticação no servidor. Qualquer requisição, sem
token e sem login, é atendida.

**Passos (barra de endereço / aba anônima):**
1. Abra uma **aba anônima** (para provar que não há sessão/login).
2. Acesse diretamente:
   - `http://localhost:8080/api/admin/professores`
   - `http://localhost:8080/api/admin/alunos`
3. A API devolve **200** e os dados (nome, e-mail, CPF) em JSON — sem login.

**Prova adicional (opcional, muito visual):** o próprio `seed-demo.sh` criou
professor, turma, alunos e envios **sem nenhum login** — todas as chamadas são
`curl` sem token.

**O que dizer:**
- O "login" da aplicação é decorativo: guarda o nome no `localStorage` do
  navegador, mas o servidor não emite token nem cria sessão.
- É a vulnerabilidade **raiz**: a V-03 (IDOR) só é tão grave porque não há
  autenticação por trás.
- **Correção (em `main`):** Spring Security + JWT, com `/api/**` exigindo
  token.

---

## 3. V-03 — IDOR: acesso a dados de outro aluno (Crítica)

**Ideia:** os endpoints de aluno recebem o `id` pela URL e só verificam se o
aluno **existe** — nunca se o solicitante é **o dono** daquele id.

**Passos (barra de endereço):**
1. Entre como **Alice** (`http://localhost:8080/index.html`, alice@sigea.com / alice123) — id **3**.
2. Troque o id na URL da API para o do **Bruno** (id **4**):
   - `http://localhost:8080/api/aluno/3/turmas`  → turmas da Alice
   - `http://localhost:8080/api/aluno/4/turmas`  → **turmas do Bruno** (id alheio)
   - `http://localhost:8080/api/aluno/3/atividades/1/envio` → **o envio e a nota da Alice**
3. Bastou **incrementar o número** para enumerar qualquer aluno da base.

> O mesmo padrão vale para `GET /api/aluno/{id}/desempenho` (notas e faltas):
> o id vem do path e nunca é confrontado com a identidade do solicitante.
> No cenário do seed não há notas acadêmicas lançadas, então use os endpoints
> acima, que já têm dados.

**O que dizer:**
- A identidade deveria vir do token, não do path. Aqui, quem escolhe o id é o
  atacante.
- Com os endpoints `DELETE` equivalentes, dá para **apagar** matrícula/envio de
  terceiros (não faça ao vivo — apenas mencione).
- **Correção (em `main`):** derivar a identidade do token e validar
  propriedade em cada operação (negar por padrão).

---

## 4. V-01 — Senhas armazenadas em texto claro (Crítica)

**Ideia:** o método chamado `hashSenha` não aplica hash nenhum — apenas
concatena o prefixo `HASH_` à senha em texto claro.

**Passos (Console H2, no navegador):**
1. Acesse `http://localhost:8080/h2-console`.
2. Preencha:
   - **JDBC URL:** `jdbc:h2:file:./sigea`
   - **User Name:** `sa`
   - **Password:** *(em branco)*
3. Conecte e execute:
   ```sql
   SELECT usuario_id, nome, email, cpf, senha_hash, perfil FROM Usuarios;
   ```
4. A coluna `senha_hash` mostra `HASH_admin123`, `HASH_senha123`,
   `HASH_alice123`… — **a senha real é legível removendo o prefixo `HASH_`**.

**O que dizer:**
- O nome do método e da coluna sugerem proteção que não existe.
- Note que **todos os professores** compartilham `HASH_senha123` (um único
  ponto de falha) — isso também toca a V-10 (credenciais padrão).
- O fato do console H2 estar aberto com `sa` e senha em branco é a **V-04**
  (console exposto), que serve de veículo para esta demonstração.
- **Correção (em `main`):** Argon2id via Spring Security, com salt por usuário;
  e o console H2 desabilitado fora de desenvolvimento.

---

## Resumo do ciclo (para fechar a apresentação)

> Nesta versão vulnerável, um aluno **rouba dados de colegas** (V-03) porque a
> API **não autentica ninguém** (V-02); se vazar o banco, todas as **senhas
> estão legíveis** (V-01); e a **IA aplica notas fraudulentas** por prompt
> injection. Todas as quatro já têm correção implementada na branch `main`.
