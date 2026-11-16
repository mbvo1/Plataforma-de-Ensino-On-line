-- =======================================
-- Projeto 2, Fase 4: log de seguranca com hash encadeado
-- =======================================
-- Fecha a lacuna da V-11: evidencias/V-11/analise.md descrevia este log
-- como implementado (migration V26), mas ele nunca existiu no codigo.
--
-- hash_atual = SHA-256 da serializacao canonica de todas as outras colunas
-- da linha, incluindo hash_anterior (ver CadeiaHashLog). A primeira linha usa
-- 64 zeros como hash_anterior.
--
-- log_id e AUTO_INCREMENT como pede o brief, mas a aplicacao informa o valor
-- no INSERT: o log_id entra no hash, entao precisa ser conhecido antes da
-- gravacao. O RegistradorLogSeguranca usa (ultimo log_id + 1) sob a mesma
-- trava que serializa a cadeia. Efeito colateral bom: duas instancias
-- gravando ao mesmo tempo colidem na PK em vez de bifurcar a cadeia em
-- silencio.
--
-- timestamp em UTC, sem fuso.
--
-- Nao ha trigger impedindo UPDATE/DELETE: o criterio de aceite da fase e
-- justamente editar uma linha a mao e ver o endpoint apontar o log_id. A
-- cadeia torna a edicao evidente; nao a impede.

CREATE TABLE log_seguranca (
    log_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp     TIMESTAMP    NOT NULL,
    tipo_evento   VARCHAR(50)  NOT NULL,
    usuario_id    BIGINT,
    recurso       VARCHAR(255),
    ip            VARCHAR(45),
    resultado     VARCHAR(20)  NOT NULL,
    detalhe       TEXT,
    hash_anterior VARCHAR(64)  NOT NULL,
    hash_atual    VARCHAR(64)  NOT NULL
);

-- Exportacao por periodo (desde/ate) para o Python da Fase 5.
CREATE INDEX idx_log_seguranca_timestamp ON log_seguranca (timestamp);
