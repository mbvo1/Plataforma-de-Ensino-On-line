-- Atualiza a constraint de tipo de eventos para aceitar novos valores
-- Remove a constraint antiga
ALTER TABLE Eventos DROP CONSTRAINT CONSTRAINT_D8;

-- Adiciona a nova constraint com os novos valores permitidos
ALTER TABLE Eventos ADD CONSTRAINT check_tipo_evento 
    CHECK (tipo IN ('PROVA', 'ENTREGA_ATIVIDADE', 'FERIADO', 'PERIODO_MATRICULA', 'TODOS', 'ALUNOS', 'PROFESSORES'));
