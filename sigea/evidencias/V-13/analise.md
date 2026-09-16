# V-13 — XSS Armazenado (nome do aluno)

## Ativo afetado
Sessão do navegador de qualquer professor que abrir a tela de correção

## Ameaça
Aluno mal-intencionado cadastrando um nome contendo script malicioso

## Vulnerabilidade
atividade-detalhes.html (renderizarTabelaEnvios) insere o campo
"nome do aluno" diretamente via innerHTML, sem escape, ao montar a
tabela de entregas

## Impacto
Execução de script arbitrário no navegador do professor - pode ser
usado para roubo de sessão, captura de token, ou ações em nome do
professor autenticado

## Probabilidade
Alta - qualquer aluno pode se cadastrar com um nome malicioso; o ataque
dispara automaticamente quando o professor abre a atividade

## Risco
Alto

## Categoria (PDF da disciplina)
XSS

## Justificativa da seleção
Categoria explicitamente listada no PDF; é a única vulnerabilidade do
inventário que envolve execução de código no navegador de outro usuário,
diferente das demais que são falhas de acesso/configuração no servidor

## Antes da correção
- Código vulnerável: atividade-detalhes.html, função
  renderizarTabelaEnvios(), interpolação direta de aluno.nomeAluno
  dentro de template string atribuída a innerHTML
- Evidência: antes-alerta.png (alerta disparado ao abrir o modal de
  entregas), antes-tabela-icone-quebrado.png (a tabela renderizada,
  mostrando o ícone de imagem quebrada no lugar do nome do aluno)
- Impacto observado: alerta disparado no navegador do professor ao
  abrir a lista de entregas de uma atividade real

## Depois da correção
- Código corrigido: atividade-detalhes.html, adicionada função
  escapeHtml() e aplicada em aluno.nomeAluno dentro de
  renderizarTabelaEnvios()
- Justificativa técnica: sanitização de saída (output encoding) - a
  técnica usada (atribuir a textContent e ler de volta via innerHTML)
  converte automaticamente caracteres HTML especiais em suas entidades
  equivalentes, impedindo que o navegador interprete a string como
  marcação executável
- Mecanismo de proteção utilizado: escape de HTML antes de qualquer
  interpolação de dado do usuário em innerHTML

## Reteste
- Mesmo payload da tentativa de ataque original, reenviado contra a
  versão corrigida
- Resultado: o payload aparece como texto literal na tabela, sem
  nenhum script executado (depois.png)
- Conclusão: Ataque → Correção → Script neutralizado, exibido como
  texto inofensivo (confirmado)