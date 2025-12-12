-- V15: Cria tabela de atividades por turma
DROP TABLE IF EXISTS AtividadesTurma;

CREATE TABLE AtividadesTurma (
    atividade_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    turma_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    arquivo_path VARCHAR(500),
    prazo TIMESTAMP NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_atividades_turma FOREIGN KEY (turma_id) REFERENCES Turmas(turma_id),
    CONSTRAINT fk_atividades_professor FOREIGN KEY (professor_id) REFERENCES Usuarios(usuario_id)
);
