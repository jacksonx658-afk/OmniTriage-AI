CREATE TABLE atendimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensagem VARCHAR(255) NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    nivel_urgencia VARCHAR(20) NOT NULL,
    resposta VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL
);