-- =======================================
-- Projeto 2, Fase 4: verdade conhecida do volume experimental
-- =======================================
-- Um registro por evento de log_seguranca que pertence a um incidente
-- injetado pelo seeder (SeedEventosExperimentais). Evento sem linha aqui e
-- uso normal.
--
-- Fica numa tabela a parte, e nao numa coluna de log_seguranca, de
-- proposito: a exportacao que o detector da Fase 5 consome nao pode conter
-- o rotulo, senao o detector so precisaria le-lo. O rotulo sai por outro
-- endpoint, usado so na avaliacao (precisao, recall, F1).
--
-- ON DELETE CASCADE: o seeder recria o volume do zero a cada execucao, e um
-- rotulo sem o evento correspondente nao tem significado.

CREATE TABLE rotulo_evento_experimental (
    log_id       BIGINT      PRIMARY KEY,
    incidente_id INT         NOT NULL,
    cenario      VARCHAR(50) NOT NULL,
    FOREIGN KEY (log_id) REFERENCES log_seguranca (log_id) ON DELETE CASCADE
);
