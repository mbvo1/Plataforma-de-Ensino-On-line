-- Migration V8: Remove períodos extras e mantém apenas 2025.2 como padrão

-- Primeiro, atualiza todas as disciplinas para apontar para o período 2025.2
UPDATE Disciplinas 
SET periodo_id = (SELECT id FROM periodos WHERE nome = '2025.2' LIMIT 1)
WHERE periodo_id IS NOT NULL;

-- Remove períodos extras, mantendo apenas 2025.2
DELETE FROM periodos WHERE nome != '2025.2';

-- Garante que 2025.2 está como ATIVO
UPDATE periodos 
SET status = 'ATIVO',
    data_inicio = NULL,
    data_fim = NULL,
    inscricao_inicio = NULL,
    inscricao_fim = NULL
WHERE nome = '2025.2';
