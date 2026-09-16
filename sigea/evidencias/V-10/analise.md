# V-10 — Credenciais padrão fixas

## Ativo afetado
Contas privilegiadas (admin e professores)

## Ameaça
Qualquer pessoa com acesso ao repositório (público ou não) ou ao
código-fonte

## Vulnerabilidade
Credencial do admin publicada em texto claro no README.md (linha 56).
A migration V12__corrigir_senhas_professores.sql atribui a mesma senha
("senha123") a todos os professores de uma vez. O endpoint real de
criação de professor (UsuariosAdminController.criarProfessor) também
usava senha fixa "senha123", em vez de gerar algo único.

## Impacto
Acesso administrativo trivial a partir de informação pública do
repositório. Comprometer um professor comprometia todos, já que a
senha era idêntica entre eles.

## Probabilidade
Alta - a informação estava documentada e pública, não exigia nenhuma
técnica de ataque

## Risco
Alto

## Categoria (PDF da disciplina)
Falhas de autenticação / Falhas de configuração

## Justificativa da seleção
Complementa a V-01 (hash de senha) - mesmo com Argon2id correto, uma
senha previsível e compartilhada anula boa parte da proteção
criptográfica

## Antes da correção
- Código vulnerável: README.md linha 56 (credencial do admin);
  V12__corrigir_senhas_professores.sql (senha única para todos);
  UsuariosAdminController.criarProfessor() (senha fixa "senha123")
- Evidência: antes-readme.png, antes-migration.png
- Impacto observado: qualquer leitor do repositório tinha a senha do
  admin; qualquer professor comprometido revelava a senha de todos os
  outros

## Depois da correção
- Código corrigido: credencial removida do README.md;
  UsuariosAdminController.criarProfessor() e resetarSenhaProfessor()
  agora geram senha aleatória via gerarSenhaProvisoria() (padrão
  "Temp" + UUID); a senha gerada é devolvida na resposta da API para o
  admin repassar ao usuário
- Justificativa técnica: cada conta deve ter uma credencial única e
  imprevisível - comprometer uma conta não deve comprometer as demais
- Mecanismo de proteção utilizado: geração de senha aleatória por
  conta, combinada com Argon2id (já corrigido na V-01) para o
  armazenamento

## Reteste
- Criação de novo professor via POST /api/admin/professores retorna
  senhaProvisoria única e imprevisível a cada chamada (Tempe91410f1)
  (depois-senha-aleatoria.png)
- Resultado: senha não é mais fixa nem compartilhada entre contas