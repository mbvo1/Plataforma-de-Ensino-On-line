# V-01 — Senha armazenada em texto claro

## Ativo afetado
Credenciais de todos os usuarios do sistema

## Ameaça
Qualquer pessoa com acesso ao banco de dados (via console H2 exposto,
backup, ou dump do arquivo .mv.db)

## Vulnerabilidade
`AutenticacaoService.hashSenha()` concatena "HASH_" a frente da senha em
texto claro, em vez de aplicar uma funcao de hash de verdade

## Impacto
Comprometimento total de qualquer conta, incluindo a de administrador.
Como usuarios reutilizam senha entre servicos, o impacto extrapola a
propria aplicacao.

## Probabilidade
Alta - basta ter acesso de leitura ao banco (que, via V-04, esta
publicamente exposto)

## Risco
Critico

## Categoria (PDF da disciplina)
Ausencia ou utilizacao inadequada de criptografia

## Justificativa da selecao
E a vulnerabilidade que sustenta o requisito obrigatorio 9 e 10 do PDF
(mecanismo de protecao criptografica e funcao hash) - hoje nao existe
nenhuma criptografia real na aplicacao.

## Antes da correcao
- Codigo vulneravel: AutenticacaoService.java, metodo hashSenha()
- Evidencia: antes.png - coluna senha_hash com 4 senhas legiveis
- Impacto observado: senha real de admin, professor e dois alunos
  visivel sem nenhum esforco de decodificacao

  ## Depois da correção
- Código corrigido: Senha.java (Argon2PasswordEncoder), 
  AutenticacaoService.java, UsuarioService.java, PerfilAlunoController.java
- Justificativa técnica: Argon2id é uma KDF (função de derivação de chave)
  deliberadamente lenta e cara em memória, ao contrário de um hash rápido
  como SHA-256 - isso encarece ataques de força bruta mesmo se o banco
  vazar por completo. Salt aleatório por senha impede ataques de rainbow
  table.
- Mecanismo de proteção utilizado: Argon2id (salt 16 bytes, hash 32 bytes,
  memória 16 MB, 2 iterações - parametros padrao do Spring Security 5.8)

## Reteste
- Consulta direta ao banco: senha_hash agora ilegivel
  (depois-hash-ilegivel.png)
- Login com senha original continua funcionando (depois-login-funcionando.png)
- Resultado: Ataque → Correção → Dado protegido, uso legítimo preservado