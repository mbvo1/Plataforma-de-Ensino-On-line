ALTER TABLE periodos ADD COLUMN data_inicio DATE;
ALTER TABLE periodos ADD COLUMN data_fim DATE;
ALTER TABLE periodos ADD COLUMN inscricao_inicio DATE;
ALTER TABLE periodos ADD COLUMN inscricao_fim DATE;

-- Atualizar período existente com datas padrão (2025.1)
UPDATE periodos 
SET data_inicio = '2025-02-03',
    data_fim = '2025-07-05',
    inscricao_inicio = '2025-01-02',
    inscricao_fim = '2025-02-01'
WHERE nome = '2025.1';
