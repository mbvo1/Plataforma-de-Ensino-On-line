# SIGEA - Plataforma de Ensino On-line

## 🚀 Sobre o Projeto

SIGEA é uma plataforma educacional integrada para a gestão de instituições de ensino, projetada para otimizar tanto os aspectos acadêmicos formais quanto os ambientes colaborativos de aprendizado. O sistema oferece ferramentas para a gestão completa de usuários (administradores, professores e alunos), organização da estrutura acadêmica (disciplinas e períodos letivos), controle de matrículas e a publicação centralizada de materiais, atividades e comunicados.

O objetivo principal é fornecer à comunidade acadêmica — administradores, professores e alunos — um ambiente digital unificado e robusto que aprimore a experiência de ensino e aprendizagem. A plataforma permite o planejamento de atividades, a distribuição de conteúdo didático e o monitoramento completo do progresso dos alunos, incluindo notas, participação em fóruns e entrega de trabalhos.

---

## ⚙️ Como Rodar o Projeto

### Opção 1: Usando Docker (Recomendado)

#### Pré-requisitos

* **Docker** e **Docker Compose** instalados

#### Passo a Passo

1. **Construir e iniciar o container:**

```bash
docker-compose up --build
```

2. **Acessar a Aplicação:**

Após iniciar, você poderá acessar:

* **Frontend**: `http://localhost:8080` - Aluno, `http://localhost:8080/login-professor.html` - Professor, `http://localhost:8080/login-admin.html` - Admin
  * Obs: login de Admin - E-mail: admin@sigea.com, CPF: 00000000000, Senha: admin123
* **API REST**: `http://localhost:8080/api`
* **Console H2 Database**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:/app/sigea`
  - Username: `sa`
  - Password: (deixe em branco)

3. **Parar o container:**

```bash
docker-compose down
```

4. **Reiniciar o container:**

```bash
docker-compose restart
```

#### Observações

* O banco de dados H2 e os arquivos enviados são persistidos em volumes Docker
* O ambiente Docker é isolado e não interfere com seu sistema local
* A primeira execução pode demorar devido ao download das dependências Maven

---

### Opção 2: Execução Local (Sem Docker)

#### Pré-requisitos

* **JDK 17** (ou superior)
* **Apache Maven** 3.8 (ou superior)

#### Passo a Passo

##### 1. Compilar o Projeto

Navegue até a pasta raiz do projeto (`sigea/`) e execute:

```bash
cd sigea
mvn clean install
```

Este comando irá:
- Compilar todos os módulos do projeto
- Executar todos os testes automatizados
- Gerar os arquivos JAR necessários

Ao final da execução, o build deve ser concluído com **`BUILD SUCCESS`**, indicando que todas as 18 especificações de teste passaram.

##### 2. Executar a Aplicação

Após a compilação bem-sucedida, execute o backend Spring Boot:

```bash
cd apresentacao-backend
mvn spring-boot:run
```

Ou, alternativamente, execute diretamente o JAR gerado:

```bash
java -jar apresentacao-backend/target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar
```

##### 3. Acessar a Aplicação

Após iniciar a aplicação, você poderá acessar:

* **Frontend**: `http://localhost:8080` - Aluno, `http://localhost:8080/login-professor.html` - Professor, `http://localhost:8080/login-admin.html` - Admin
  * Obs: login de Admin - E-mail: admin@sigea.com, CPF: 00000000000, Senha: admin123
* **API REST**: `http://localhost:8080/api`
* **Console H2 Database**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./sigea`
  - Username: `sa`
  - Password: (deixe em branco)

##### 4. Executar Apenas os Testes

Para executar apenas os testes sem recompilar tudo:

```bash
cd sigea
mvn test
```

#### Observações

* O banco de dados H2 é criado automaticamente na primeira execução
* As migrações do Flyway são executadas automaticamente ao iniciar a aplicação
* Os arquivos enviados são salvos na pasta `apresentacao-backend/uploads/`

---

## 📋 Documentação Adicional

* **[Padrões de Projeto](padroes.md)** - Documentação completa dos padrões de projeto adotados no projeto

---

## 👥 Integrantes

* Felipe Bandeira
* Luis Felipe Arruda
* Marcelo Bresani
* Marcelo Henrique

---

## 🔗 Links Gerais

* **[Apresentação](https://www.canva.com/design/DAG2U5n0a3I/-9maNTx9KBascFk08bwTjw/edit?utm_content=DAG2U5n0a3I&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)**
* **[Protótipo](https://www.figma.com/design/mshXnITEmNfVZrYsQT8gV6/Untitled?node-id=0-1&p=f&t=C1KQSf8LGKrchoLk-0)**
* **[Mapa de Histórias do Usuário](https://miro.com/app/board/uXjVJ7Q1pVU=)**
* **[Descrição do Domínio](https://docs.google.com/document/d/19dmEHKq8BIhEIaaKKtwoF_yDFlVeOprotMjORMVZwSE/edit?tab=t.0)**
