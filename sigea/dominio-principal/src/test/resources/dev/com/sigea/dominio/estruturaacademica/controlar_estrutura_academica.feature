# language: pt
Funcionalidade: Controlar Estrutura Acadêmica
  Como um Administrador do sistema SIGEA
  Eu quero criar e gerenciar Períodos Letivos e Disciplinas
  Para estruturar a oferta acadêmica da instituição

  Contexto:
    Dado que sou um Administrador logado

  Cenário: Admin cria um novo período letivo com sucesso
    Quando eu defino as datas de início como "01/02/2025" e fim como "30/06/2025" para o novo período letivo "2025.1"
    Então um novo período letivo "2025.1" com status "ATIVO" deve existir no sistema

  Cenário: Admin tenta criar período letivo com datas sobrepostas
    Dado que já existe um período letivo com datas de "01/02/2025" a "30/06/2025"
    Quando eu tento criar um novo período letivo com início em "01/06/2025" e fim em "31/07/2025"
    Então o sistema deve me impedir com a mensagem "Já existe um período letivo ativo nas datas especificadas"
    E nenhum novo período letivo deve ser criado

  Cenário: Admin cria uma nova disciplina com sucesso
    Quando eu crio uma nova disciplina com o nome "Banco de Dados"
    Então a disciplina "Banco de Dados" deve ser criada com sucesso

  Cenário: Admin tenta criar disciplina com nome duplicado
    Dado que já existe uma disciplina com o nome "Cálculo Vetorial"
    Quando eu tento criar uma nova disciplina com o nome "Cálculo Vetorial"
    Então o sistema deve me impedir com a mensagem "Já existe uma disciplina com este nome"