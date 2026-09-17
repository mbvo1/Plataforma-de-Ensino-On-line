-- Corrige o hash do admin padrao, que a migration V2 gravou no formato
-- antigo (HASH_admin123), incompativel com a verificacao Argon2id da V-01.
-- Hash abaixo corresponde a senha "admin123" em Argon2id (mesmos parametros
-- usados pelo Argon2PasswordEncoder padrao do Spring Security).
UPDATE Usuarios
SET senha_hash = '$argon2id$v=19$m=16384,t=2,p=1$OjHgxToErCyDX0jfzyZKBg$eCdNYoXp32ETLJKTSC5leY2n2NVxjHFaFYFQR4lyB2A'
WHERE email = 'admin@sigea.com' AND senha_hash = 'HASH_admin123';