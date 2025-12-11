-- Insere um período letivo padrão caso não exista
INSERT INTO PeriodosLetivos (identificador, data_inicio, data_fim, data_inicio_matricula, data_fim_matricula, status)
SELECT '2025.1', '2025-02-01', '2025-06-30', '2025-01-15', '2025-02-15', 'ABERTO'
WHERE NOT EXISTS (SELECT 1 FROM PeriodosLetivos WHERE identificador = '2025.1');
