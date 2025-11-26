# language: pt
Funcionalidade: Gerenciar Fóruns de Discussão
  Como um membro da comunidade acadêmica
  Eu quero usar os fóruns para tirar dúvidas e discutir
  Para melhorar o aprendizado colaborativo

  Cenário: Aluno matriculado cria um novo tópico com sucesso
    Dado que a aluna "aluna-mariana" está matriculada na disciplina "DISC-ESOFT"
    Quando a aluna "aluna-mariana" cria um tópico com o título "Dúvida sobre a Atividade 3" no fórum da disciplina "DISC-ESOFT"
    Então o tópico "Dúvida sobre a Atividade 3" deve ser criado com sucesso

  Cenário: Aluno não matriculado tenta acessar fórum de disciplina
    Dado que o aluno "aluno-lucas" não está matriculado na disciplina "DISC-FISICA"
    Quando o aluno "aluno-lucas" tenta criar um tópico no fórum da disciplina "DISC-FISICA"
    Então o sistema deve retornar um erro de acesso não autorizado com a mensagem "Acesso não autorizado. Você não está matriculado nesta disciplina."