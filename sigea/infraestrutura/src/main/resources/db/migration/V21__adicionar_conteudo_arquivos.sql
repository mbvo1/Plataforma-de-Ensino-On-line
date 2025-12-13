-- Adiciona coluna para armazenar o conteúdo do arquivo em Base64 na tabela de avisos
ALTER TABLE AvisosTurma ADD COLUMN IF NOT EXISTS arquivo_conteudo LONGTEXT;

-- Adiciona coluna para armazenar o conteúdo do arquivo em Base64 na tabela de atividades
ALTER TABLE AtividadesTurma ADD COLUMN IF NOT EXISTS arquivo_conteudo LONGTEXT;
