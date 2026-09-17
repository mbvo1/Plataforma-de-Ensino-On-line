# SIGEA - Plataforma de Ensino On-line

## 🚀 Sobre o Projeto

SIGEA é uma plataforma educacional integrada para a gestão de instituições de ensino, projetada para otimizar tanto os aspectos acadêmicos formais quanto os ambientes colaborativos de aprendizado. O sistema oferece ferramentas para a gestão completa de usuários (administradores, professores e alunos), organização da estrutura acadêmica (disciplinas e períodos letivos), controle de matrículas e a publicação centralizada de materiais, atividades e comunicados.

O objetivo principal é fornecer à comunidade acadêmica — administradores, professores e alunos — um ambiente digital unificado e robusto que aprimore a experiência de ensino e aprendizagem. A plataforma permite o planejamento de atividades, a distribuição de conteúdo didático e o monitoramento completo do progresso dos alunos, incluindo notas, participação em fóruns e entrega de trabalhos.

---

## 🔒 SecureAI Lab — Auditoria de Segurança

Este repositório foi reutilizado como base de estudo para a disciplina **Cibersegurança Aplicada a Dados e IA** (CESAR School), num ciclo completo de **construção → análise → exploração → correção → reteste**, em vez de partir de uma aplicação com vulnerabilidades plantadas artificialmente.

**Aplicação publicada:** [plataforma-de-ensino-on-line-production.up.railway.app](https://plataforma-de-ensino-on-line-production.up.railway.app)
*(o banco de dados é reiniciado a cada novo deploy; se estiver vazio, use a tela "Cadastre-se" para criar uma conta de teste)*

**Relatório técnico:** `[adicionar link aqui quando finalizado]`

### As 14 vulnerabilidades trabalhadas

Cada uma tem ciclo completo de ataque, correção e reteste documentado em `sigea/evidencias/<ID>/`.

| ID | Vulnerabilidade | Categoria |
|---|---|---|
| IA | Prompt Injection no feedback automático | Segurança de IA |
| V-01 | Senha armazenada em texto claro | Criptografia |
| V-02 | API sem autenticação | Autenticação |
| V-03 | IDOR no perfil do aluno | Autorização |
| V-04 | Console H2 exposto publicamente | Configuração |
| V-05 | Upload de arquivo inseguro | Upload de arquivos |
| V-06 | Controle de acesso por papel ausente (RBAC) | Autorização |
| V-07 | CPF armazenado sem cifragem | Criptografia |
| V-08 | CORS permissivo | Configuração / API |
| V-09 | Dados pessoais expostos em log | Exposição de dados |
| V-10 | Credenciais padrão fixas | Autenticação / Configuração |
| V-11 | Ausência de log de segurança | Integridade / Configuração |
| V-12 | Comando destrutivo do Flyway habilitado | Configuração |
| V-13 | XSS armazenado (sistêmico, 30+ arquivos) | XSS |

### Controle de versão desta fase

- **Tag `v1-vulneravel`**: marca o estado da aplicação antes de qualquer correção de segurança, permitindo gerar `git diff` entre essa tag e a `main` como evidência verificável de cada correção.
- **Branches `fix/V-XX-nome`**: uma por vulnerabilidade, todas já mescladas na `main`.
- **Branch `demo-apresentacao`**: ⚠️ parte deliberadamente da tag `v1-vulneravel` e reintroduz 4 vulnerabilidades (IA, V-01, V-02, V-03) de propósito, para permitir demonstração ao vivo dos ataques durante a apresentação oral. **Esta branch nunca deve ser mesclada na `main`.**

---

## ⚙️ Como Rodar o Projeto

### Pré-requisitos

* **JDK 17** (ou superior)
* **Apache Maven** 3.8 (ou superior)

### Variáveis de ambiente obrigatórias

A aplicação não sobe sem essas três variáveis configuradas no ambiente:

| Variável | Finalidade |
|---|---|
| `JWT_SECRET` | Assinatura dos tokens de autenticação (HMAC-SHA512) |
| `GEMINI_API_KEY` | Chave da API do Google Gemini, usada na funcionalidade de feedback por IA |
| `CPF_ENCRYPTION_KEY` | Chave de cifragem do CPF em repouso (AES-GCM) |

Gere um valor aleatório para cada uma (exemplo em PowerShell):
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```
Configure como variável de ambiente do sistema/usuário (ou exporte no terminal antes de rodar), e reinicie o terminal/IDE antes de continuar. `GEMINI_API_KEY` precisa ser uma chave real, obtida no Google AI Studio.

### Passo a Passo

#### 1. Compilar o Projeto

Navegue até a pasta raiz do projeto (`sigea/`) e execute:

```bash
cd sigea
mvn clean install
```

Este comando irá:
- Compilar todos os módulos do projeto
- Executar todos os testes automatizados
- Gerar os arquivos JAR necessários

Ao final da execução, o build deve ser concluído com **`BUILD SUCCESS`**.

#### 2. Executar a Aplicação

Após a compilação bem-sucedida, execute o backend Spring Boot:

```bash
cd apresentacao-backend
mvn spring-boot:run
```

Ou, alternativamente, execute diretamente o JAR gerado:

```bash
java -jar apresentacao-backend/target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar
```

#### 3. Acessar a Aplicação

* **Aluno**: `http://localhost:8080/`
* **Professor**: `http://localhost:8080/login-professor.html`
* **Administrador**: `http://localhost:8080/login-admin.html`
* **API REST**: `http://localhost:8080/api`

Obs: login de Admin - credencial não fica documentada em texto claro por motivo de segurança (ver V-10 no inventário de vulnerabilidades). O console H2 foi desabilitado por padrão (ver V-04); reative `spring.h2.console.enabled=true` apenas temporariamente e em ambiente local para inspecionar o banco.

#### 4. Executar Apenas os Testes

Para executar apenas os testes sem recompilar tudo:

```bash
cd sigea
mvn test
```

### Observações

* O banco de dados H2 é criado automaticamente na primeira execução
* As migrações do Flyway são executadas automaticamente ao iniciar a aplicação
* Os arquivos enviados são salvos na pasta `apresentacao-backend/uploads/`

---

## 📋 Documentação Adicional

* **[Padrões de Projeto](padroes.md)** - Documentação completa dos padrões de projeto adotados no projeto
* **[Evidências de segurança](sigea/evidencias/)** - Ataque, correção e reteste de cada uma das 14 vulnerabilidades

---

## 👥 Integrantes

* Felipe Bandeira
* Rodrigo Marques
* Marcelo Bresani
* Paulo Portella
* Rodrigo Nunes
* Joao Victor Nunes

---

## 🔗 Links Gerais

* **[Apresentação](https://www.canva.com/design/DAG2U5n0a3I/-9maNTx9KBascFk08bwTjw/edit?utm_content=DAG2U5n0a3I&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)**
* **[Protótipo](https://www.figma.com/design/mshXnITEmNfVZrYsQT8gV6/Untitled?node-id=0-1&p=f&t=C1KQSf8LGKrchoLk-0)**
* **[Mapa de Histórias do Usuário](https://miro.com/app/board/uXjVJ7Q1pVU=)**
* **[Descrição do Domínio](https://docs.google.com/document/d/19dmEHKq8BIhEIaaKKtwoF_yDFlVeOprotMjORMVZwSE/edit?tab=t.0)**
