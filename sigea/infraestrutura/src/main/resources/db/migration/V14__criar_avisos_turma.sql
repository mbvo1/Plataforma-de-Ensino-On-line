-- Remove a tabela de relacionamento Aviso_Turmas (não utilizada)
DROP TABLE IF EXISTS Aviso_Turmas;

-- Cria tabela específica para avisos de turma (tipo Google Classroom)
-- Diferente da tabela Avisos que é para avisos de calendário
CREATE TABLE AvisosTurma (
    aviso_turma_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    turma_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    arquivo_path VARCHAR(500),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (turma_id) REFERENCES Turmas(turma_id) ON DELETE CASCADE,
    FOREIGN KEY (professor_id) REFERENCES Usuarios(usuario_id)
);
