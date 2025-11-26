# language: pt
Funcionalidade: Gestão de Desempenho Acadêmico
  Como um professor
  Eu quero lançar notas para os alunos matriculados
  Para avaliar seu desempenho

  Cenário: Professor lança uma nota válida para um aluno matriculado
    Dado uma sala com o aluno "aluno-carlos" matriculado
    Quando o professor lança a nota 8 na avaliação "AV1" para o aluno "aluno-carlos"
    Então a nota 8 na "AV1" deve ser registrada para o aluno "aluno-carlos"

  Cenário: Professor tenta lançar uma nota inválida
    Dado uma sala com o aluno "aluno-carlos" matriculado
    Quando o professor tenta lançar a nota 12 na avaliação "AV1" para o aluno "aluno-carlos"
    Então o sistema deve lançar uma exceção de validação com a mensagem "Valor inválido. A nota deve ser um número entre 0 e 10."