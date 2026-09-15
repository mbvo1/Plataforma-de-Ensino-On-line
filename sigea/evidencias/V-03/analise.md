# V-03 — IDOR (Insecure Direct Object Reference)

## Ativo afetado
Dados pessoais de qualquer aluno (nome, e-mail, CPF)

## Ameaça
Um aluno autenticado (com login válido, mas sem permissão sobre outros
registros) consultando dados de terceiros

## Vulnerabilidade
`PerfilAlunoController.buscarPerfil()` busca o usuário pelo `alunoId` do
path e devolve o dado, sem nunca comparar esse id com a identidade de
quem fez a requisição

## Impacto
Vazamento de CPF e e-mail de qualquer aluno da instituição — violação
direta da LGPD (dado pessoal exposto a terceiro sem base legal)

## Probabilidade
Alta — só exige estar logado com qualquer conta e trocar um número na URL

## Risco
Crítico

## Categoria (PDF da disciplina)
Falhas de autorização / Controle de acesso inadequado

## Justificativa da seleção
Autenticação (V-02) sozinha não basta — aqui provamos que, mesmo sabendo
quem é o usuário, o sistema não verifica *o que* ele tem permissão de ver

## Antes da correção
- Código vulnerável: PerfilAlunoController.java, método buscarPerfil(),
  sem nenhuma verificação de propriedade
- Evidência: antes.png — token do aluno 34 usado para ler o perfil
  completo da Maria (id 65)
- Impacto observado: nome, e-mail e CPF da Maria expostos integralmente

## Depois da correção
- Código corrigido: PerfilAlunoController.java — método privado
  semPermissaoSobre() adicionado, chamado no início de buscarPerfil()
  e atualizarPerfil()
- Justificativa técnica: princípio do menor privilégio — a identidade
  extraída do JWT (via SecurityContextHolder) é comparada ao recurso
  solicitado antes de qualquer acesso a dado. ADMIN e PROFESSOR mantêm
  acesso amplo, por necessidade legítima de função.
- Mecanismo de proteção utilizado: verificação de propriedade
  (ownership check) em nível de aplicação, complementando a
  autenticação já implementada na V-02

## Reteste
- GET em perfil de terceiro: 403 Forbidden (depois-bloqueado.png)
- GET no próprio perfil: 200 OK, funciona normalmente (depois-autorizado.png)
- PUT em perfil de terceiro: 403 Forbidden (depois-put-bloqueado.png)
- Resultado: Ataque → Correção → Ataque bloqueado (confirmado), inclusive
  na operação de escrita (PUT), que era ainda mais grave que a leitura