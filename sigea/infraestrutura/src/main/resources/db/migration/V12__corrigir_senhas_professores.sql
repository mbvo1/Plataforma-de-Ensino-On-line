-- =======================================
-- Corrigir senhas dos professores existentes
-- =======================================
-- Atualiza todas as senhas de professores que estão sem o prefixo HASH_
-- Isso garante consistência com o AutenticacaoService que adiciona HASH_ ao fazer login

UPDATE Usuarios 
SET senha_hash = 'HASH_senha123'
WHERE perfil = 'PROFESSOR' 
  AND senha_hash = 'senha123';
