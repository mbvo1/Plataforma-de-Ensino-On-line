# V-02 — API sem autenticação

## Ativo afetado
Toda a API do SIGEA — dados acadêmicos, pessoais e de desempenho de
qualquer usuário

## Ameaça
Qualquer pessoa com acesso de rede à aplicação, sem precisar de
credencial nenhuma

## Vulnerabilidade
Nenhum endpoint da API verifica autenticação. O login existe na
interface, mas não emite token nem cria sessão — a ausência de login
não impede nenhuma chamada subsequente.

## Impacto
Acesso irrestrito a qualquer dado da aplicação por qualquer pessoa

## Probabilidade
Alta — não exige nenhuma habilidade técnica além de saber a URL do
endpoint

## Risco
Crítico

## Categoria (PDF da disciplina)
Falhas de autenticação

## Justificativa da seleção
É a vulnerabilidade raiz de onde derivam outras falhas (como o IDOR,
V-03) — sem autenticação, não existe base nenhuma para verificar
autorização depois.

## Antes da correção
- Código vulnerável: ausência total de Spring Security no projeto;
  `DesempenhoAlunoController` sem nenhuma anotação de segurança
- Evidência da exploração: antes.png — requisições sem login retornando
  200 OK, tanto para o id=1 (admin) quanto id=34 (aluno teste)
- Impacto observado: qualquer dado da API é acessível sem autenticação