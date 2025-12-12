-- Cria tabela para eventos criados por professores (separado de Eventos gerais)
CREATE TABLE Eventos_Professor (
    evento_professor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_evento TIMESTAMP NOT NULL,
    professor_id BIGINT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (professor_id) REFERENCES Usuarios(usuario_id)
);
