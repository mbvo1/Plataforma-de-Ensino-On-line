# language: pt
Funcionalidade: Aluno interage com a turma
  Como um aluno, eu quero ingressar em turmas e enviar atividades.

Cenário: Aluno ingressa em turma com código válido
  Dado que existe uma turma com o código de acesso "CODIGO-VALIDO"
  Quando um aluno com ID "aluno-1" tenta ingressar na turma com o código da turma criada
  Então a turma deve ter 1 participante

Cenário: Aluno tenta ingressar em turma com código inválido
  Dado que não existe nenhuma turma com o código "CODIGO-INVALIDO"
  Quando um aluno com ID "aluno-2" tenta ingressar na turma com o código "CODIGO-INVALIDO"
  Então a interação com a turma deve retornar erro "Código da turma não encontrado. Verifique o código e tente novamente."