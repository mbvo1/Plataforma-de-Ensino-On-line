-- Adiciona coluna arquivo_path na tabela Envios para armazenar o arquivo enviado pelo aluno
ALTER TABLE Envios ADD COLUMN IF NOT EXISTS arquivo_path VARCHAR(500);
