-- Migration V7: Adiciona relacionamento entre Disciplinas e Períodos
-- Permite rastrear quais disciplinas pertencem a cada período letivo

-- Adiciona coluna periodo_id na tabela Disciplinas
ALTER TABLE Disciplinas ADD COLUMN periodo_id BIGINT;

-- Adiciona foreign key para periodos
ALTER TABLE Disciplinas ADD CONSTRAINT fk_disciplina_periodo 
    FOREIGN KEY (periodo_id) REFERENCES periodos(id);

-- Atualiza disciplinas existentes para referenciar o período ativo atual
UPDATE Disciplinas 
SET periodo_id = (SELECT id FROM periodos WHERE status = 'ATIVO' LIMIT 1)
WHERE periodo_id IS NULL;
