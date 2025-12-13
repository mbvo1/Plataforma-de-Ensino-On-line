-- =======================================
-- Criação da tabela de Chamadas
-- Armazena presença/falta dos alunos por data e aula
-- =======================================

CREATE TABLE Chamadas (
    chamada_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sala_id BIGINT NOT NULL,
    matricula_id BIGINT NOT NULL,
    data_chamada DATE NOT NULL,
    falta_aula1 BOOLEAN NOT NULL DEFAULT FALSE,
    falta_aula2 BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (sala_id) REFERENCES Salas(sala_id) ON DELETE CASCADE,
    FOREIGN KEY (matricula_id) REFERENCES Matriculas(matricula_id) ON DELETE CASCADE,
    
    -- Garante uma única chamada por aluno por dia
    UNIQUE(sala_id, matricula_id, data_chamada)
);

-- Índice para melhorar performance nas consultas por data
CREATE INDEX IDX_CHAMADAS_DATA ON Chamadas(data_chamada);
CREATE INDEX IDX_CHAMADAS_SALA_DATA ON Chamadas(sala_id, data_chamada);

