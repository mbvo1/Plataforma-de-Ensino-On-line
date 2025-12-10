-- Adiciona colunas na tabela Disciplinas existente
ALTER TABLE Disciplinas ADD COLUMN IF NOT EXISTS periodo VARCHAR(10);
ALTER TABLE Disciplinas ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ATIVO';
ALTER TABLE Disciplinas ADD COLUMN IF NOT EXISTS salas_ofertadas INTEGER DEFAULT 0;

-- Atualiza constraint de status
ALTER TABLE Disciplinas DROP CONSTRAINT IF EXISTS check_disciplina_status;
ALTER TABLE Disciplinas ADD CONSTRAINT check_disciplina_status CHECK (status IN ('ATIVO', 'INATIVO'));

-- Criação da tabela de períodos letivos simplificada
CREATE TABLE IF NOT EXISTS periodos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT check_periodo_status CHECK (status IN ('ATIVO', 'ENCERRADO'))
);

-- Inserir período padrão
INSERT INTO periodos (nome, status) VALUES ('2025.2', 'ATIVO');

