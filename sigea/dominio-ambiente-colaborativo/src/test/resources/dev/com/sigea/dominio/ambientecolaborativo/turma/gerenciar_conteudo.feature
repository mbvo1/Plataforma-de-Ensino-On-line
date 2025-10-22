# language: pt
Funcionalidade: Gerenciar Conteúdo Didático
  Como um professor
  Eu quero publicar materiais de estudo em minhas turmas
  Para disponibilizar conteúdos aos alunos

  Cenário: Professor publica um material com anexo em sua turma
    Dado que o professor "prof-ana" possui uma turma chamada "Engenharia de Software"
    Quando o professor "prof-ana" publica na turma um novo material com título "Slides Aula 1", descrição "Introdução a DDD" e anexa o arquivo "aula1.pdf"
    Então a turma "Engenharia de Software" deve conter um material com o título "Slides Aula 1"
    E esse material deve ter um anexo chamado "aula1.pdf"

  Cenário: Professor tenta publicar material em turma de outro professor
    Dado que o professor "prof-ana" possui uma turma chamada "Engenharia de Software"
    E existe outro professor com ID "prof-joao"
    Quando o professor "prof-joao" tenta publicar um material na turma do professor "prof-ana"
    Então o sistema deve lançar uma exceção de segurança com a mensagem "Apenas o professor criador da turma pode publicar materiais."