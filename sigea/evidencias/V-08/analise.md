# V-08 — CORS permissivo

## Ativo afetado
Toda a API do SIGEA

## Ameaça
Um site malicioso controlado por um atacante, capaz de fazer chamadas
à API do navegador de uma vítima que o visite

## Vulnerabilidade
`@CrossOrigin(origins = "*")` presente em 15 controllers, permitindo que
qualquer origem, mesmo forjada, receba resposta liberada via cabeçalho
Access-Control-Allow-Origin

## Impacto
Um site de terceiros pode ler respostas da API no contexto do navegador
da vítima. O vetor real de exploração é limitado, já que a autenticação
usa JWT via header Authorization (não cookie), que um site de terceiro
não consegue anexar automaticamente - mas a configuração em si continua
sendo uma falha de defesa em profundidade

## Probabilidade
Média - a exploração completa depende de outro vetor (ex: o token já
estar acessível ao script malicioso via XSS)

## Risco
Médio

## Categoria (PDF da disciplina)
Falhas de configuração / Falhas em APIs

## Justificativa da seleção
Complementa a analise de superficie de ataque da API - mesmo nao sendo
critica isoladamente, agrava o impacto de outras vulnerabilidades (ex:
um XSS que capture o token pode exfiltrar dados livremente, sem
restricao de origem)

## Antes da correção
- Codigo vulneravel: @CrossOrigin(origins = "*") em 15 controllers
- Evidencia: antes.png - Access-Control-Allow-Origin: * mesmo com origem
  forjada e inexistente
- Impacto observado: qualquer origem, real ou inventada, e liberada
  pelo servidor

  ## Depois da correção
- Código corrigido: removida a anotação @CrossOrigin(origins = "*")
  dos 15 controllers que a possuíam
- Justificativa técnica: o frontend é servido pelo mesmo domínio da API
  (mesma origem), então nenhuma configuração de CORS é necessária -
  o navegador já permite chamadas same-origin por padrão. Adicionar CORS
  aberto era desnecessário e ampliava a superfície de ataque sem
  benefício algum.
- Mecanismo de proteção utilizado: remoção da funcionalidade
  desnecessária (mesmo princípio da V-04 - menor superfície de ataque)

## Reteste
- Requisição com origem forjada: cabeçalhos Access-Control-Allow-Origin
  e Vary: Origin ausentes na resposta (depois.png)
- Resultado: Ataque → Correção → Comportamento CORS removido (confirmado)