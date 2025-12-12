-- Migration V17: Corrige foreign key de Salas para referenciar a tabela periodos correta
-- A tabela Salas estava referenciando PeriodosLetivos que não é mais usada
-- Agora referencia a tabela periodos que é a tabela ativa

-- Remove a foreign key antiga que referencia PeriodosLetivos
-- O H2 pode gerar nomes diferentes para constraints, então precisamos encontrar e remover
-- Vamos usar uma abordagem que funciona: recriar a tabela sem a constraint antiga

-- Primeiro, vamos tentar remover a constraint pelo nome que aparece no erro
-- Se não existir, o comando será ignorado (dependendo da versão do H2)
ALTER TABLE Salas DROP CONSTRAINT CONSTRAINT_4B0;

-- Adiciona nova foreign key que referencia a tabela periodos correta
ALTER TABLE Salas ADD CONSTRAINT FK_SALAS_PERIODOS 
    FOREIGN KEY (periodo_letivo_id) REFERENCES periodos(id);

