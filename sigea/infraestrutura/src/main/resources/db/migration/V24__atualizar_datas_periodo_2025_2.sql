-- Atualizar período padrão 2025.2 com as datas especificadas
UPDATE periodos 
SET data_inicio = '2025-07-01',
    data_fim = '2025-12-13',
    inscricao_inicio = '2025-07-01',
    inscricao_fim = '2025-07-01'
WHERE nome = '2025.2';

