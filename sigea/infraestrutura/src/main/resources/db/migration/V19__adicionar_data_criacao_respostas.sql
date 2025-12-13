-- Adiciona coluna data_criacao na tabela Respostas para rastrear quando o comentário foi criado

ALTER TABLE Respostas ADD COLUMN data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
