# V-04 — Console H2 exposto sem autenticação

## Ativo afetado
Todo o banco de dados da aplicação

## Ameaça
Qualquer pessoa com acesso de rede ao servidor

## Vulnerabilidade
`spring.h2.console.enabled=true`, rota `/h2-console` publicamente
acessível, com usuário `sa` e senha em branco documentados no README

## Impacto
Acesso SQL irrestrito - leitura, escrita e DDL sobre toda a base

## Probabilidade
Alta - a rota é previsível e nao exige nenhuma credencial

## Risco
Crítico

## Categoria (PDF da disciplina)
Falhas de configuração

## Justificativa da seleção
É o vetor que amplifica todas as outras vulnerabilidades - mesmo depois
de corrigir V-01, V-02 e V-03, um console de banco aberto permite
contornar essas proteções lendo/editando dados diretamente

## Antes da correção
- Código vulnerável: application.properties,
  spring.h2.console.enabled=true sem nenhuma restrição de acesso
- Evidência: antes.png - console conectado e executando SQL livremente
- Impacto observado: leitura completa da tabela Usuarios sem autenticação

## Depois da correção
- Código corrigido: application.properties,
  spring.h2.console.enabled=false
- Justificativa técnica: princípio de menor superfície de ataque - uma
  ferramenta administrativa de banco de dados não deveria estar acessível
  pela mesma porta pública da aplicação, especialmente sem autenticação
  própria
- Mecanismo de proteção utilizado: desabilitação da funcionalidade
  (nenhum controle de acesso é tão eficaz quanto simplesmente não expor
  a superfície)

## Reteste
- Acesso a /h2-console: 403 Forbidden (depois.png)
- Resultado: Ataque → Correção → Ataque bloqueado (confirmado)