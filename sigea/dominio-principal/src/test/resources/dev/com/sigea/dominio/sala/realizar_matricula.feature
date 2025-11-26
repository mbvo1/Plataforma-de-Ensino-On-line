# language: pt
Funcionalidade: Realizar Matrícula
  Como um aluno
  Eu quero me matricular em uma sala de uma disciplina
  Para poder cursá-la oficialmente

  Cenário: Aluno se matricula em sala com vaga disponível
    Dado uma sala da disciplina "Engenharia de Software" com 1 vaga
    E um aluno com ID "aluno-joao"
    Quando o aluno "aluno-joao" se matricula na sala
    Então a matrícula deve ser confirmada
    E a sala deve ter 0 vagas restantes

  Cenário: Aluno tenta se matricular em sala lotada
    Dado uma sala da disciplina "Cálculo" com 0 vagas
    E um aluno com ID "aluno-maria"
    Quando o aluno "aluno-maria" tenta se matricular na sala
    Então o processo de matrícula deve falhar com a mensagem "Não há vagas disponíveis para esta sala."