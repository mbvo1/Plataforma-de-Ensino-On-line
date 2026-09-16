# V-12 — Flyway clean habilitado

## Ativo afetado
Todo o schema e dados do banco de dados

## Ameaça
Qualquer pessoa com acesso ao ambiente de execução (ou a uma ferramenta
administrativa) capaz de disparar o comando `flyway:clean`

## Vulnerabilidade
`spring.flyway.clean-disabled=false` no application.properties permite
que o comando `flyway:clean` seja executado, apagando todo o schema

## Impacto
Perda total de dados por erro operacional ou ação maliciosa

## Probabilidade
Baixa - não é explorável remotamente pela API, exige acesso direto ao
ambiente ou a uma ferramenta de build/administração

## Risco
Baixo (mas impacto catastrófico caso ocorra)

## Categoria (PDF da disciplina)
Falhas de configuração

## Justificativa da seleção
É uma configuração perigosa deixada com o valor permissivo por padrão,
sem necessidade - o Flyway já recomenda desabilitar isso fora de
ambiente de desenvolvimento

## Antes da correção
- Código vulnerável: application.properties,
  spring.flyway.clean-disabled=false
- Evidência: antes.png
- Impacto potencial: comando flyway:clean apagaria todo o schema, sem
  possibilidade de demonstração segura em ambiente compartilhado

  ## Depois da correção
- Código corrigido: spring.flyway.clean-disabled=true
- Justificativa técnica: princípio de menor privilégio - não há motivo
  operacional para essa funcionalidade destrutiva estar habilitada por
  padrão em nenhum ambiente além de desenvolvimento local isolado
- Mecanismo de proteção utilizado: desabilitação da funcionalidade

## Reteste
- Aplicação continua subindo e funcionando normalmente com a flag
  desabilitada (depois.png)
- Resultado: risco de perda total de dados eliminado