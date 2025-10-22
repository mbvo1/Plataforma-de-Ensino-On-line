# language: pt

Funcionalidade: Gerenciar Avisos e Comunicados
  Como um membro da comunidade acadêmica (professor ou administrador),
  Eu quero poder criar e visualizar avisos,
  Para que a comunicação seja eficiente e centralizada.

  Contexto: Atores e turmas pré-existentes no sistema
    Dado que existe um professor com email "carlos.souza@sigea.com.br" e senha "senha123"
    E que o professor "carlos.souza@sigea.com.br" é responsável pela turma "Cálculo I"
    E que existe um outro professor com email "ana.clara@sigea.com.br"

  Cenário: Professor publica um novo aviso para sua turma
    Quando o professor "carlos.souza@sigea.com.br" publica um aviso na turma "Cálculo I" com o título "Aula de revisão" e conteúdo "Pessoal, teremos uma aula de revisão na sexta-feira."
    Então um aviso com título "Aula de revisão" deve ser criado
    E o aviso deve estar associado à turma "Cálculo I"

  Cenário: Professor não pode publicar aviso em turma que não é sua
    Quando o professor "ana.clara@sigea.com.br" tenta publicar um aviso na turma "Cálculo I" com o título "Aviso importante"
    Então o sistema deve lançar uma exceção de "Acesso Negado" com a mensagem "O usuário não tem permissão para publicar avisos nesta turma."