-- Corrige a foreign key da tabela Envios para referenciar AtividadesTurma

-- Remove a foreign key antiga que aponta para Atividades
ALTER TABLE Envios DROP CONSTRAINT IF EXISTS CONSTRAINT_7A3;

-- Remove qualquer outra FK que possa existir para atividade_id
ALTER TABLE Envios DROP CONSTRAINT IF EXISTS FK_ENVIOS_ATIVIDADE;

-- Adiciona a nova foreign key apontando para AtividadesTurma
ALTER TABLE Envios ADD CONSTRAINT FK_ENVIOS_ATIVIDADES_TURMA 
    FOREIGN KEY (atividade_id) REFERENCES AtividadesTurma(atividade_id) ON DELETE CASCADE;

-- Adiciona coluna para armazenar o conteúdo do arquivo em base64
ALTER TABLE Envios ADD COLUMN IF NOT EXISTS arquivo_conteudo LONGTEXT;
