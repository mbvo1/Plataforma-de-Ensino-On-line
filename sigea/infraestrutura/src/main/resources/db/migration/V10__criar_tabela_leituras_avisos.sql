-- Criação da tabela de leituras de avisos
CREATE TABLE aviso_leituras (
    leitura_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aviso_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_leitura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (aviso_id) REFERENCES Avisos(aviso_id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(usuario_id) ON DELETE CASCADE,
    
    UNIQUE (aviso_id, usuario_id)
);

-- Índices para melhorar performance
CREATE INDEX idx_leituras_usuario ON aviso_leituras(usuario_id);
CREATE INDEX idx_leituras_aviso ON aviso_leituras(aviso_id);
