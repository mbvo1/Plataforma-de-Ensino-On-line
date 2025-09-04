# Descrição do Domínio: Plataforma de Ensino Online
- 1. Introdução
A Plataforma de Ensino Online tem como objetivo oferecer uma experiência completa de educação digital, permitindo que alunos se inscrevam em cursos, acompanhem seu progresso e recebam certificados. O sistema também possibilita que instrutores criem e gerenciem conteúdos de forma organizada, enquanto a administração supervisiona usuários, pagamentos e relatórios.

Este projeto foi desenvolvido como parte da disciplina Requisitos, Projeto de Software e Validação, adotando os princípios de Domain-Driven Design (DDD), Arquitetura Limpa e padrões de projeto.
O documento descreve o domínio nos níveis preliminar e estratégico do DDD, por meio da definição da linguagem onipresente e da separação de subdomínios. Os níveis tático e operacional serão abordados nos modelos (Context Mapper, agregados, repositórios) e na implementação em Java com Spring Boot, JPA e Angular/Vaadin.

2. Atores do Domínio
Aluno → Realiza cadastro, adquire cursos, acompanha progresso, participa de avaliações e emite certificados.
Instrutor → Cria e gerencia cursos, organiza módulos e aulas, aplica avaliações e interage com os alunos em fóruns.
Administrador → Controla permissões de usuários, supervisiona pagamentos e gera relatórios financeiros e estatísticos.

3. Conceitos Centrais
Curso – Unidade de ensino composta por módulos, aulas e avaliações.
Módulo – Subdivisão de um curso, agrupando aulas relacionadas.
Aula – Conteúdo disponibilizado pelo instrutor (vídeo, PDF, material interativo).
Avaliação – Atividade de medição de aprendizado (quiz, prova objetiva ou dissertativa).
Matrícula – Vínculo entre aluno e curso, podendo ter prazo limitado.
Certificado – Documento digital emitido automaticamente após a conclusão do curso.
Pagamento – Processo de inscrição em cursos, com suporte a cupons, parcelamento e reembolso.
Fórum – Espaço colaborativo para interação entre alunos e instrutores.

4. Subdomínios
O sistema foi dividido em subdomínios, cada um refletindo uma área funcional clara:

Autenticação e Perfis → Cadastro e login de usuários, com papéis (Aluno, Instrutor, Admin).
Gestão de Cursos → Criação, edição e organização de cursos, módulos e aulas.
Matrículas e Pagamentos → Inscrições, regras de acesso, controle de prazos e transações financeiras.
Aprendizado e Avaliações → Acompanhamento do progresso, provas e correções.
Certificação → Emissão e validação de certificados digitais.
Comunidade → Fóruns de dúvidas e interação entre usuários.

5. Regras de Negócio Gerais

O aluno só pode acessar o próximo módulo após concluir o anterior.
Certificados são emitidos apenas quando o curso for concluído e as avaliações aprovadas.
Cupons de desconto possuem prazo de validade e quantidade máxima de uso.
Cursos podem ter prazos de acesso definidos (ex.: 6 meses), expiram automaticamente após o período.
Instrutores não podem excluir cursos que possuam alunos ativos matriculados.
Reembolso pode ser solicitado apenas dentro de até 7 dias da compra.
Avaliações podem ter pesos diferentes na nota final do curso.

6. Narrativa do Domínio

A Plataforma de Ensino Online organiza o ciclo de vida da educação digital. Os alunos podem se cadastrar, adquirir cursos, acessar conteúdos estruturados em módulos e realizar avaliações. O progresso é monitorado de forma sequencial, garantindo que a trilha pedagógica definida pelo instrutor seja respeitada.

Ao finalizar o curso com sucesso, o sistema gera automaticamente um certificado digital com código único de validação, possibilitando comprovação segura da formação.

Instrutores possuem autonomia para criar cursos, disponibilizar materiais e interagir com alunos através de fóruns e avaliações. Administradores supervisionam o funcionamento geral, controlam permissões e monitoram transações financeiras.

Esse conjunto de funcionalidades reflete um ecossistema integrado de ensino digital, modelado de acordo com os princípios de DDD. A descrição aqui cobre os níveis preliminar (linguagem onipresente) e estratégico (subdomínios e contextos), estabelecendo a base para a modelagem tática e operacional que será desenvolvida nas próximas etapas.
