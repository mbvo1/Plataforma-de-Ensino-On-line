-- =======================================
-- CONTEXTO: IDENTIDADE_E_ACESSO
-- =======================================

CREATE TABLE Usuarios (
    usuario_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,

    perfil VARCHAR(50) NOT NULL 
        CHECK (perfil IN ('ALUNO', 'PROFESSOR', 'ADMINISTRADOR')),
    
    status VARCHAR(50) NOT NULL 
        CHECK (status IN ('ATIVO', 'INATIVO'))
);


-- =======================================
-- CONTEXTO: GESTAO_ACADEMICA
-- =======================================

CREATE TABLE Disciplinas (
    disciplina_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_disciplina VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT
);

CREATE TABLE Disciplina_PreRequisitos (
    disciplina_id BIGINT NOT NULL,
    pre_requisito_id BIGINT NOT NULL,
    PRIMARY KEY (disciplina_id, pre_requisito_id),
    FOREIGN KEY (disciplina_id) REFERENCES Disciplinas(disciplina_id) ON DELETE CASCADE,
    FOREIGN KEY (pre_requisito_id) REFERENCES Disciplinas(disciplina_id) ON DELETE CASCADE
);

CREATE TABLE PeriodosLetivos (
    periodo_letivo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificador VARCHAR(100) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    data_inicio_matricula DATE NOT NULL,
    data_fim_matricula DATE NOT NULL,
    
    status VARCHAR(50) NOT NULL 
        CHECK (status IN ('ABERTO', 'ENCERRADO', 'MATRICULAS_ABERTAS'))
);

CREATE TABLE Salas (
    sala_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disciplina_id BIGINT NOT NULL,
    periodo_letivo_id BIGINT NOT NULL,
    identificador VARCHAR(100) NOT NULL,
    professor_responsavel_id BIGINT NOT NULL, 
    horario VARCHAR(255),
    limite_de_vagas INT NOT NULL,
    
    FOREIGN KEY (disciplina_id) REFERENCES Disciplinas(disciplina_id),
    FOREIGN KEY (periodo_letivo_id) REFERENCES PeriodosLetivos(periodo_letivo_id),
    FOREIGN KEY (professor_responsavel_id) REFERENCES Usuarios(usuario_id)
);

CREATE TABLE Matriculas (
    matricula_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sala_id BIGINT NOT NULL, 
    aluno_id BIGINT NOT NULL, 
    data_matricula DATE NOT NULL,
    total_faltas INT NOT NULL DEFAULT 0,
    
    status VARCHAR(50) NOT NULL 
        CHECK (status IN ('ATIVA', 'CANCELADA')),
        
    situacao VARCHAR(50) NOT NULL DEFAULT 'CURSANDO'
        CHECK (situacao IN ('APROVADO', 'REPROVADO', 'CURSANDO')),
    
    FOREIGN KEY (sala_id) REFERENCES Salas(sala_id),
    FOREIGN KEY (aluno_id) REFERENCES Usuarios(usuario_id)
);

CREATE TABLE Avaliacoes (
    avaliacao_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricula_id BIGINT NOT NULL,
    nome_avaliacao VARCHAR(100) NOT NULL, 
    valor DOUBLE, 
    
    FOREIGN KEY (matricula_id) REFERENCES Matriculas(matricula_id) ON DELETE CASCADE,
    UNIQUE(matricula_id, nome_avaliacao)
);

CREATE TABLE Topicos (
    topico_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disciplina_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL, 
    titulo VARCHAR(255) NOT NULL,
    conteudo TEXT,
    
    FOREIGN KEY (disciplina_id) REFERENCES Disciplinas(disciplina_id),
    FOREIGN KEY (autor_id) REFERENCES Usuarios(usuario_id)
);

CREATE TABLE Respostas (
    resposta_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topico_id BIGINT NOT NULL, 
    autor_id BIGINT NOT NULL, 
    conteudo TEXT NOT NULL,
    verificada_pelo_professor BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (topico_id) REFERENCES Topicos(topico_id) ON DELETE CASCADE,
    FOREIGN KEY (autor_id) REFERENCES Usuarios(usuario_id)
);


-- =======================================
-- CONTEXTO: AMBIENTE_COLABORATIVO
-- =======================================

CREATE TABLE Turmas (
    turma_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    codigo_acesso VARCHAR(50) UNIQUE, 
    professor_criador_id BIGINT NOT NULL, 
    
    FOREIGN KEY (professor_criador_id) REFERENCES Usuarios(usuario_id)
);

CREATE TABLE Turma_Alunos (
    turma_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    PRIMARY KEY (turma_id, aluno_id),
    FOREIGN KEY (turma_id) REFERENCES Turmas(turma_id) ON DELETE CASCADE,
    FOREIGN KEY (aluno_id) REFERENCES Usuarios(usuario_id) ON DELETE CASCADE
);

CREATE TABLE Atividades (
    atividade_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    turma_id BIGINT NOT NULL, 
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_de_entrega TIMESTAMP,
    
    FOREIGN KEY (turma_id) REFERENCES Turmas(turma_id) ON DELETE CASCADE
);

CREATE TABLE Envios (
    envio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atividade_id BIGINT NOT NULL, 
    aluno_id BIGINT NOT NULL, 
    data_envio TIMESTAMP NOT NULL,
    feedback_professor TEXT,
    nota DOUBLE, 
    
    status VARCHAR(50) NOT NULL 
        CHECK (status IN ('ENVIADO', 'ATRASADO', 'CORRIGIDO')),
        
    FOREIGN KEY (atividade_id) REFERENCES Atividades(atividade_id) ON DELETE CASCADE,
    FOREIGN KEY (aluno_id) REFERENCES Usuarios(usuario_id)
);


-- =======================================
-- CONTEXTO: COMUNICACAO_GERAL
-- =======================================

CREATE TABLE Avisos (
    aviso_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    conteudo TEXT NOT NULL,
    autor_id BIGINT NOT NULL, 
    
    alvo_tipo VARCHAR(50) NOT NULL 
        CHECK (alvo_tipo IN ('GERAL', 'ALUNOS', 'PROFESSORES', 'SALA_ESPECIFICA', 'TURMA_ESPECIFICA')),
        
    FOREIGN KEY (autor_id) REFERENCES Usuarios(usuario_id)
);

CREATE TABLE Aviso_Salas (
    aviso_id BIGINT NOT NULL,
    sala_id BIGINT NOT NULL,
    PRIMARY KEY (aviso_id, sala_id),
    FOREIGN KEY (aviso_id) REFERENCES Avisos(aviso_id) ON DELETE CASCADE,
    FOREIGN KEY (sala_id) REFERENCES Salas(sala_id) ON DELETE CASCADE
);

CREATE TABLE Aviso_Turmas (
    aviso_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    PRIMARY KEY (aviso_id, turma_id),
    FOREIGN KEY (aviso_id) REFERENCES Avisos(aviso_id) ON DELETE CASCADE,
    FOREIGN KEY (turma_id) REFERENCES Turmas(turma_id) ON DELETE CASCADE
);

CREATE TABLE Eventos (
    evento_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    data_evento TIMESTAMP NOT NULL,
    responsavel_id BIGINT, 
    
    tipo VARCHAR(50) NOT NULL 
        CHECK (tipo IN ('PROVA', 'ENTREGA_ATIVIDADE', 'FERIADO', 'PERIODO_MATRICULA')),
        
    FOREIGN KEY (responsavel_id) REFERENCES Usuarios(usuario_id)
);

