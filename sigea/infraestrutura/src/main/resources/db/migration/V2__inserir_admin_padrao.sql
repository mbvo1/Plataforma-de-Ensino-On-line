-- =======================================
-- Inserir Usuário Administrador Padrão
-- =======================================

-- Inserir admin com senha: admin123
-- CPF: 000.000.000-00 (deve ser alterado)
INSERT INTO Usuarios (nome, email, cpf, senha_hash, perfil, status) 
VALUES ('Administrador', 'admin@sigea.com', '00000000000', 'HASH_admin123', 'ADMINISTRADOR', 'ATIVO');
