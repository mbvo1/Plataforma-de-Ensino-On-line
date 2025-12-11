-- Adiciona coluna status na tabela Salas
ALTER TABLE Salas ADD COLUMN status VARCHAR(20) DEFAULT 'ATIVO';

-- Atualiza todas as salas existentes para ATIVO
UPDATE Salas SET status = 'ATIVO' WHERE status IS NULL;
