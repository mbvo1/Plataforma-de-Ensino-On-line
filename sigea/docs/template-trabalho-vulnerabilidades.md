# Template de Trabalho — Ciclo Completo por Vulnerabilidade

Este documento é o roteiro que cada pessoa (ou dupla) segue para a(s)
vulnerabilidade(s) que ficou(aram) sob sua responsabilidade. Seguir a mesma
ordem e o mesmo formato garante que as 14 fiquem no mesmo nível de
profundidade, e que ninguém esqueça uma etapa.

---

## Antes de começar

- [ ] Confirme qual(is) vulnerabilidade(s) do inventário são suas (ver tabela
      de atribuição no final deste documento)
- [ ] Puxe a `main` atualizada: `git pull origin main`
- [ ] Crie sua branch de trabalho:
  ```powershell
  git checkout -b fix/V-0X-nome-curto
  ```
  Exemplo: `fix/V-01-hash-senha`, `fix/V-03-idor`
- [ ] Crie sua pasta de evidências, se ainda não existir:
  ```powershell
  mkdir evidencias/V-0X
  ```

---

## Etapa 1 — Ataque (capturar o "antes")

Siga o passo a passo específico da sua vulnerabilidade no
`guia-exploracao-vulnerabilidades.md`. Ao final desta etapa, você deve ter:

- [ ] Um screenshot ou log de terminal mostrando o ataque funcionando
- [ ] Salvo em `evidencias/V-0X/antes.png` (ou `.txt`)
- [ ] Commitado:
  ```powershell
  git add evidencias/V-0X/
  git commit -m "V-0X: evidencia do ataque (antes da correcao)"
  ```

**Não pule esta etapa mesmo se a correção parecer óbvia.** Sem a evidência
do "antes", perde-se a nota de exploração (15%) e de reteste (5%) mesmo que
a correção esteja perfeita.

---

## Etapa 2 — Preencher a ficha de análise

Copie o modelo abaixo para um arquivo `evidencias/V-0X/analise.md` e
preencha cada campo. Isso vira insumo direto para a Matriz de Riscos e para
a seção do relatório.

```markdown
# V-0X — [nome da vulnerabilidade]

## Ativo afetado
[Que dado ou funcionalidade está em risco? Ex: dados de desempenho
acadêmico de todos os alunos]

## Ameaça
[Quem exploraria isso, e com que motivação? Ex: um aluno mal-intencionado
buscando ver notas de colegas]

## Vulnerabilidade
[Descrição técnica curta da falha]

## Impacto
[O que acontece se for explorada? Ex: vazamento de dado pessoal sob a LGPD]

## Probabilidade
[Alta / Média / Baixa — e por quê]

## Risco (impacto x probabilidade)
[Crítico / Alto / Médio / Baixo]

## Categoria (OWASP / PDF da disciplina)
[Ex: Controle de acesso quebrado — A01]

## Justificativa da seleção
[Por que esta categoria é relevante para o SIGEA especificamente — o PDF
exige essa justificativa técnica, "porque é mais seguro" não é aceito]
```

---

## Etapa 3 — Corrigir

- [ ] Implemente a correção na sua branch
- [ ] Não altere nada fora do escopo da sua vulnerabilidade (evita conflito
      de merge com as outras duplas)
- [ ] Rode `mvn clean install` e confirme que compila
- [ ] Faça commit **separado** do commit da evidência:
  ```powershell
  git add [arquivos alterados]
  git commit -m "V-0X: corrige [nome da vulnerabilidade]"
  ```

Isso mantém o histórico do Git limpo: um commit "antes" (evidência), um
commit "depois" (correção) — o `git diff` entre eles vira prova direta no
relatório.

---

## Etapa 4 — Reteste

- [ ] Repita **exatamente o mesmo ataque** da Etapa 1, contra o código já
      corrigido
- [ ] Capture a evidência de que agora ele falha/é bloqueado
- [ ] Salve em `evidencias/V-0X/depois.png` (ou `.txt`)
- [ ] Commit:
  ```powershell
  git add evidencias/V-0X/
  git commit -m "V-0X: evidencia do reteste (ataque bloqueado apos correcao)"
  ```

---

## Etapa 5 — Preencher a ficha de correção

Complete o mesmo arquivo `evidencias/V-0X/analise.md` com mais esta seção:

```markdown
## Antes da correção
- Código/configuração vulnerável: [caminho do arquivo + trecho]
- Evidência da exploração: [link para antes.png/antes.txt]
- Impacto observado: [o que a evidência mostrou]

## Depois da correção
- Código/configuração corrigida: [caminho do arquivo + trecho]
- Justificativa técnica: [por que essa correção, referenciando um conceito
  da ementa — Tríade CIA, Zero Trust, LGPD, criptografia, etc. Nunca
  "porque é mais seguro" sozinho]
- Mecanismo de proteção utilizado: [nome técnico — ex: Argon2id, RBAC,
  parametrização de query]

## Reteste
- Evidência: [link para depois.png/depois.txt]
- Resultado: Ataque → Correção → Ataque bloqueado (confirmado)
```

---

## Etapa 6 — Merge

- [ ] Suba sua branch: `git push origin fix/V-0X-nome-curto`
- [ ] Abra um Pull Request para a `main`
- [ ] Peça para outra pessoa do grupo revisar antes de mesclar (evita dois
      PRs quebrando um ao outro por mexerem no mesmo arquivo)
- [ ] Após aprovado, confirme o merge

---

## Checklist final por vulnerabilidade

Antes de considerar a sua concluída, confirme:

- [ ] `evidencias/V-0X/antes.png` (ou .txt) existe
- [ ] `evidencias/V-0X/depois.png` (ou .txt) existe
- [ ] `evidencias/V-0X/analise.md` está completo (todos os campos preenchidos)
- [ ] Existe pelo menos 1 commit de evidência "antes" e 1 de correção,
      separados
- [ ] O código corrigido está mesclado na `main`
- [ ] Você consegue explicar, em 30 segundos, o ataque e a correção para
      alguém do grupo — se não conseguir, ainda não está pronto para a
      apresentação

---

## Tabela de atribuição

| Vulnerabilidade | Responsável(is) | Branch | Status |
|---|---|---|---|
| V-01 — Senha em texto claro (crítica) | Marcelo | | ⬜ |
| V-02 — API sem autenticação (crítica) | Marcelo | | ⬜ |
| V-03 — IDOR (crítica) | Marcelo | | ⬜ |
| V-04 — Console H2 exposto (crítica) | Marcelo | | ⬜ |
| V-05 — Upload sem validação | *livre — avise no grupo* | | ⬜ |
| V-06 — Sem RBAC | *livre — avise no grupo* | | ⬜ |
| V-07 — CPF sem cifragem | *livre — avise no grupo* | | ⬜ |
| V-08 — CORS permissivo | *livre — avise no grupo* | | ⬜ |
| V-09 — Dados pessoais em log | *livre — avise no grupo* | | ⬜ |
| V-10 — Credenciais padrão | *livre — avise no grupo* | | ⬜ |
| V-11 — Sem log de segurança | *livre — avise no grupo* | | ⬜ |
| V-12 — Flyway clean habilitado | *livre — avise no grupo* | | ⬜ |
| V-13 — XSS armazenado | *livre — avise no grupo* | | ⬜ |
| IA — Prompt injection | Marcelo | feature/ia-feedback | 🔵 |

**Regra para pegar uma vulnerabilidade livre:** avise no grupo qual você vai
pegar, **antes** de começar, para duas pessoas não atacarem a mesma. Depois
de avisar, siga o passo a passo deste documento a partir de "Antes de
começar".

Status: ⬜ não iniciado · 🔵 ataque feito · 🟡 corrigido · 🟢 retestado e mesclado
