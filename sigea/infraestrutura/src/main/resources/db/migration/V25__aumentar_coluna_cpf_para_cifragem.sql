-- =======================================
-- Mitigacao V-07: CPF passa a ser cifrado em repouso (AES-256-GCM)
-- =======================================
-- O texto cifrado (nonce + ciphertext + tag, em Base64, com prefixo "v1:")
-- e bem maior que os 14 caracteres de um CPF em texto claro. Linhas
-- existentes continuam legiveis como texto claro (ver CpfCryptoConverter)
-- e sao recifradas na proxima vez que forem salvas; esta migration so
-- amplia a coluna, sem reescrever dado nenhum.
ALTER TABLE Usuarios ALTER COLUMN cpf VARCHAR(255);
