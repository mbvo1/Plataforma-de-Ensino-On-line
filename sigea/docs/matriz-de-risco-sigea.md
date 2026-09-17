# Matriz de Risco: SIGEA (estado real, pós-correções)

> Item 3 dos "Próximos passos" do
> [`inventario-vulnerabilidades-sigea.md`](inventario-vulnerabilidades-sigea.md):
> "Converter este inventário em matriz de riscos, acrescentando probabilidade
> e impacto por ativo."

**Data da análise:** 2026-09-16 (mais recente).
**Branch analisada:** `main`, commit `6c3f044` (após os PRs #1 a #13).
**Método:** leitura do código-fonte atual de cada vulnerabilidade (não do
texto do inventário original, que foi escrito antes das correções e não foi
atualizado por quem corrigiu). Nada aqui foi assumido a partir de mensagem
de commit ou de documento de evidência sem conferir o código correspondente.

---

## Por que este documento existe separado do inventário

O inventário original (`inventario-vulnerabilidades-sigea.md`) registra o
estado do código antes de qualquer correção, exceto nas entradas V-05,
V-06 e V-07 (que eu mesmo revalidei dinamicamente). As outras dez entradas
continuam com a descrição de quando a vulnerabilidade foi descoberta, mesmo
que o código já tenha mudado.

Ao conferir o estado atual, encontrei uma discrepância importante:
**duas correções alegadas em mensagem de commit não existem de fato no
código** (V-10 e V-11, detalhado abaixo). Isso muda a forma de calcular
risco aqui: não posso tratar "existe um PR chamado V-XX, mergeado" como
sinônimo de "corrigido". Cada linha desta matriz reflete o que o código
realmente faz hoje, verificado por leitura direta (e, em alguns casos,
grep para provar ausência).

---

## Escala usada

**Probabilidade** (chance de exploração dado o estado atual do código):

| Nível | Descrição |
|---|---|
| 1: Muito baixa | Requer acesso que a aplicação hoje não expõe remotamente (ex.: acesso direto ao arquivo do banco) |
| 2: Baixa | Requer condição adicional não trivial (ex.: linha específica ainda não recifrada, ou conta de papel específico) |
| 3: Média | Explorável por qualquer usuário autenticado, mas exige mais de um passo ou não é óbvio |
| 4: Alta | Explorável por qualquer usuário autenticado (inclusive autorregistrado) em uma requisição direta |
| 5: Muito alta | Explorável sem autenticação nenhuma, ou informação já pública no repositório |

**Impacto** (consequência se explorado):

| Nível | Descrição |
|---|---|
| 1: Muito baixo | Sem efeito sobre dado pessoal, integridade ou disponibilidade |
| 2: Baixo | Efeito limitado a um recurso não sensível |
| 3: Médio | Exposição de dado pessoal de um subconjunto de usuários, ou efeito reversível |
| 4: Alto | Exposição ou adulteração de dado pessoal/acadêmico em escala, ou escalação de privilégio |
| 5: Muito alto | Comprometimento total de confidencialidade, integridade ou disponibilidade da base |

**Risco = Probabilidade × Impacto**, faixas:

| Faixa | Rótulo |
|---|---|
| 1 a 4 | Baixo |
| 5 a 9 | Médio |
| 10 a 16 | Alto |
| 17 a 25 | Crítico |

---

## Matriz visual (Probabilidade × Impacto)

Cada célula mostra os IDs cujo risco residual atual cai ali. Posição no
grid: probabilidade (linha) por impacto (coluna), ambos no estado de hoje.

| Probabilidade \ Impacto | 1 Muito baixo | 2 Baixo | 3 Médio | 4 Alto | 5 Muito alto |
|---|---|---|---|---|---|
| **5 Muito alta** | | | | | **V-06** |
| **4 Alta** | | | | V-09, V-10, V-13 | **V-03** |
| **3 Média** | | | V-11 | | |
| **2 Baixa** | | V-05 | V-01, V-07 | | |
| **1 Muito baixa** | | V-08, V-12 | | V-04 | V-02 |

Legenda de cor por faixa de risco (Probabilidade × Impacto):
🔴 Crítico (17 a 25), 🟠 Alto (10 a 16), 🟡 Médio (5 a 9), 🟢 Baixo (1 a 4).

---

## Tabela detalhada

| ID | Status verificado no código | Prob. | Impacto | Risco | Cor |
|---|---|---|---|---|---|
| V-01 | Parcialmente corrigida | 2 | 3 | 6 | 🟡 Médio |
| V-02 | Corrigida | 1 | 5 | 5 | 🟡 Médio* |
| V-03 | Parcialmente corrigida, lacuna grande | 4 | 4 | 16 | 🟠 Alto |
| V-04 | Corrigida | 1 | 5 | 5 | 🟡 Médio* |
| V-05 | Corrigida (validado por mim) | 2 | 2 | 4 | 🟢 Baixo |
| V-06 | Parcialmente corrigida, lacuna grave | 5 | 5 | 25 | 🔴 Crítico |
| V-07 | Corrigida (validado por mim) | 2 | 3 | 6 | 🟡 Médio |
| V-08 | Corrigida | 1 | 2 | 2 | 🟢 Baixo |
| V-09 | Não corrigida (branch não mergeada) | 4 | 3 | 12 | 🟠 Alto |
| V-10 | Não corrigida (achado de processo) | 4 | 4 | 16 | 🟠 Alto |
| V-11 | Não corrigida (achado de processo grave) | 4 | 3 | 12 | 🟠 Alto |
| V-12 | Corrigida | 1 | 2 | 2 | 🟢 Baixo |
| V-13 | Parcialmente corrigida, lacuna residual | 4 | 4 | 16 | 🟠 Alto |

\* V-02 e V-04 ficam em "Médio" pela combinação probabilidade muito baixa
com impacto muito alto (evento raro, mas catastrófico se acontecer). É o
comportamento esperado de uma matriz de risco, não um erro de conta. Ver
justificativa por item abaixo.

---

## Justificativa por item

### V-06: Crítico (o pior item do repositório hoje)

O fix mergeado (meu, PR #8) cobre só `UsuariosAdminController`.
`NotasController` (`POST /api/sala/{salaId}`, lançamento de notas) e
`DisciplinasPeriodosController` continuam sem nenhuma verificação de
papel. Isso significa que hoje, na `main`, um `ALUNO` recém-autorregistrado
pode chamar `POST /api/sala/{salaId}` e lançar ou sobrescrever notas de
qualquer sala, inclusive as próprias. Não precisa nem do bug de IDOR de
V-03 para isso: é uma ausência de controle de acesso em nível de função,
sem nenhuma dependência de outro ID. Este é provavelmente o achado mais
grave de todo o exercício, porque ataca o dado que dá sentido ao sistema
(nota) e está totalmente aberto.

### V-03: Alto (perto de crítico)

Confirmado por leitura de código: o padrão `semPermissaoSobre()` existe em
exatamente um arquivo (`PerfilAlunoController`). `DesempenhoAlunoController.
buscarDesempenho()` não tem nenhuma comparação entre o `alunoId` do path e
o usuário autenticado: o mesmo ataque descrito originalmente no inventário
ainda funciona hoje contra notas e faltas. `AlunoMatriculaController` e
`AtividadesAlunoController` têm o mesmo problema. Só uma fração pequena do
IDOR original foi corrigida.

### V-10: Alto (achado de processo)

Dois commits (`da216f7`, `6c3f044`) trazem a frase "remove credencial do
README" na mensagem. Conferido o diff de ambos: nenhum toca em `README.md`.
O arquivo, hoje, na linha 56, continua com `Senha: admin123` em texto
claro. Atenuante real, não deliberado: a migration
`V2__inserir_admin_padrao.sql` grava esse hash no esquema antigo
(`HASH_admin123`), que não bate mais com o verificador Argon2id introduzido
pela correção de V-01, ou seja, a senha publicada no README hoje não
funciona de fato. Isso reduz a probabilidade prática de exploração direta
(por isso não está em 5), mas não corrige o problema: é proteção
acidental, não intencional, e quebra no instante em que alguém "consertar"
o hash do admin seed sem revisar o README também.

### V-11: Alto (achado de processo mais grave do lote)

`sigea/evidencias/V-11/analise.md` descreve, com detalhe técnico e
screenshots, uma tabela `LogSeguranca`, um serviço com encadeamento
SHA-256 e um endpoint de verificação de integridade. Nenhum desses três
existe no código: migrations vão até `V25` (a minha, de V-07), e uma busca
por `LogSeguranca` ou `verificar-integridade` em todo o repositório não
retorna nada. A documentação de evidência descreve uma implementação que
não está commitada. Tratando como o que é, ausência total de log de
segurança, exatamente como no inventário original, mantenho a mesma
avaliação de impacto (amplifica todos os outros riscos, porque nenhum
incidente seria detectável), mas chamo atenção para o fato em si: é o
segundo caso (junto com V-10) de mensagem de commit ou documento de
evidência não corresponder ao código. Vale conferir manualmente qualquer
outra entrada do inventário antes de considerá-la corrigida só porque tem
uma pasta em `evidencias/`.

### V-09: Alto

Confirmado que o fix mora só em `fix/V-09-log-dados-pessoais`, nunca
mergeada (`git log origin/main | grep -i V-09` não retorna nada). Em
`main`: `spring.jpa.show-sql=true` continua ativo, despejando toda query
(com nome e e-mail em claro) no console a cada requisição. O CPF
especificamente já não vaza mais em claro por essa via, como efeito
colateral feliz da cifragem de V-07. Achei também `println`s adicionais
com nome de usuário fora do ponto citado no inventário original
(`UsuarioFactory`, alguns observers), não cobertos pelo fix pendente.

### V-13: Alto

O ponto relatado originalmente (`atividade-detalhes.html`) está corrigido
com uma função `escapeHtml`, e o mesmo padrão foi replicado em pelo menos
23 arquivos JS do frontend. Mas ainda há cerca de 12 arquivos que
interpolam dado de usuário via `innerHTML` sem chamar `escapeHtml` (ex.:
`forum-professor.js`). O vetor original (nome de aluno malicioso lido por
um professor) está fechado; vetores irmãos, no mesmo padrão de código, não
estão.

### V-01: Médio

A maioria do código usa Argon2id corretamente agora. Mas
`PerfilProfessorController` (edição do próprio perfil por um professor)
ainda grava a senha nova como `"HASH_" + senha` em texto claro reversível.
Efeito colateral real, não só teórico: um professor que troque a própria
senha por essa tela fica sem conseguir logar de novo (o verificador Argon2
rejeita o hash antigo), e a senha nova fica gravada de forma recuperável.

### V-07: Médio

Corrigido por mim (AES-256-GCM, nonce determinístico). Ressalva já
documentada: linhas gravadas antes do fix continuam em texto claro até
serem salvas de novo (sem migração em lote). E-mail permanece em claro,
decisão aceita e documentada.

### V-02 e V-04: Médio (pela combinação, não por falha)

Ambos genuinamente corrigidos e verificados. Ficam em "Médio" só porque a
matriz pondera um evento raro (probabilidade muito baixa) contra o
catastrófico que ele seria se acontecesse (impacto muito alto). Vale notar
o comentário obsoleto em `SecurityConfig.java` ("V-04 ainda nao
corrigida") ao lado do `permitAll` de `/h2-console/**`: inofensivo hoje
porque a rota está desligada por `application.properties`, mas é o tipo de
comentário que engana quem for reabilitar o H2 console futuramente sem
revisar a config de segurança junto.

### V-05, V-08, V-12: Baixo

Corrigidos e verificados (V-05 por mim, com ataque real; V-08 e V-12 por
leitura direta de `application.properties` e ausência de `@CrossOrigin`).

---

## Comparativo: severidade original vs. risco residual

| ID | Severidade original (inventário) | Risco residual hoje | Direção |
|---|---|---|---|
| V-01 | Crítica | Médio | Caiu bastante, mas não a zero |
| V-02 | Crítica | Médio (pela combinação P×I) | Efetivamente resolvido |
| V-03 | Crítica | Alto | Caiu pouco: a maior parte do problema original persiste |
| V-04 | Crítica | Médio (pela combinação P×I) | Efetivamente resolvido |
| V-05 | Alta | Baixo | Resolvido |
| V-06 | Alta | **Crítico** | **Subiu**: a superfície corrigida (admin) era menor que a que ficou aberta (notas) |
| V-07 | Alta | Médio | Resolvido, com ressalva |
| V-08 | Média | Baixo | Resolvido |
| V-09 | Média | Alto | **Subiu**: nunca foi corrigido, e o `show-sql` sozinho já é mais amplo que o `println` original |
| V-10 | Média | Alto | **Subiu**: a exposição no README nunca foi removida |
| V-11 | Média | Alto | **Subiu**: nunca foi implementado, e é a base para detectar qualquer outro incidente |
| V-12 | Baixa | Baixo | Resolvido, como esperado |
| V-13 | Alta | Alto | Caiu pouco: vetor original fechado, vetores irmãos abertos |

Quatro itens (V-06, V-09, V-10, V-11) têm risco residual maior que a
severidade com que entraram no inventário. Em três desses quatro (V-09,
V-10, V-11) a razão é simples: nunca foram corrigidos, e a classificação
"Média" original já não reflete que hoje são dos únicos itens totalmente
intocados, ao lado de um trabalho de correção que já avançou bastante no
resto do inventário. O risco relativo deles subiu porque o resto do
repositório ficou mais seguro ao redor. Em V-06, a razão é diferente e mais
séria: o fix que existe é real, mas cobre a fatia menos crítica do
problema original.

---

## Prioridade sugerida a partir desta matriz

1. **V-06 (restante):** `NotasController` e `DisciplinasPeriodosController`
   precisam do mesmo tratamento de V-06, com atenção a que
   `NotasController` não pode ser `@PreAuthorize("hasRole('ADMINISTRADOR')")`
   simples: precisa aceitar `PROFESSOR` dono da sala, no padrão de V-03.
2. **V-03 (restante):** estender `semPermissaoSobre()` (ou equivalente)
   para `DesempenhoAlunoController`, `AlunoMatriculaController` e
   `AtividadesAlunoController`.
3. **V-10:** remover de fato a credencial do `README.md`. Trivial, e já
   foi alegado como feito duas vezes sem acontecer.
4. **V-11:** implementar de fato o que `evidencias/V-11/analise.md` já
   descreve (a documentação pode ser reaproveitada como especificação).
5. **V-09:** revisar e mergear `fix/V-09-log-dados-pessoais`, ou reabrir o
   trabalho considerando o que já mudou desde que a branch foi criada
   (V-07 já mitigou parte do vazamento de CPF via `show-sql`).
6. **V-13 (restante):** aplicar `escapeHtml` nos cerca de 12 arquivos
   ainda pendentes.
7. **V-01 (restante):** corrigir `PerfilProfessorController` para usar
   `Senha.criarNova()` como o resto do código já faz.

## Nota metodológica para quem for atualizar esta matriz depois

Antes de marcar qualquer item como corrigido a partir de agora, confira o
código-fonte de verdade, não só a mensagem de commit ou um `analise.md` em
`evidencias/`. Dois dos treze itens deste inventário tinham exatamente esse
problema.
