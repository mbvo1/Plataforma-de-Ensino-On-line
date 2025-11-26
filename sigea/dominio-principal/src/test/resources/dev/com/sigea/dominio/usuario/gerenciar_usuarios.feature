# language: pt
Funcionalidade: 7 - Gerenciar Usuários
  Como um Administrador do sistema SIGEA
  Eu quero cadastrar e gerenciar usuários
  Para controlar quem pode acessar e usar a plataforma

  Contexto:
    Dado que eu sou um administrador logado no sistema

  Cenário: Admin cadastra um novo professor com sucesso
    Quando eu preencho o formulário de cadastro com o nome "Carlos Santana", o e-mail "carlos.santana@email.com" e o perfil "PROFESSOR"
    E eu clico em "Salvar"
    Então um novo usuário com o perfil "PROFESSOR" e status "ATIVO" deve ser criado
    E o usuário "carlos.santana@email.com" deve existir na lista de professores

  Cenário: Admin tenta cadastrar professor com e-mail já existente
    Dado que já existe um usuário com o e-mail "professor.existente@email.com"
    Quando eu tento criar um novo professor com o nome "Ana Maria" e o e-mail "professor.existente@email.com"
    Então o sistema deve me informar que "Este e-mail já está em uso"
    E nenhum novo usuário deve ser criado com este e-mail