-- Remove salas das disciplinas que serão deletadas
DELETE FROM Salas WHERE disciplina_id IN (SELECT disciplina_id FROM Disciplinas WHERE periodo_id IN (SELECT id FROM periodos WHERE nome != '2025.2'));

-- Remove pré-requisitos das disciplinas que serão deletadas
DELETE FROM Disciplina_PreRequisitos WHERE disciplina_id IN (SELECT disciplina_id FROM Disciplinas WHERE periodo_id IN (SELECT id FROM periodos WHERE nome != '2025.2'));

-- Remove disciplinas dos períodos que serão deletados
DELETE FROM Disciplinas WHERE periodo_id IN (SELECT id FROM periodos WHERE nome != '2025.2');

-- Deleta todos os períodos exceto 2025.2
DELETE FROM periodos WHERE nome != '2025.2';

-- Garante que 2025.2 está ATIVO
UPDATE periodos SET status = 'ATIVO', data_inicio = NULL, data_fim = NULL, inscricao_inicio = NULL, inscricao_fim = NULL WHERE nome = '2025.2';
