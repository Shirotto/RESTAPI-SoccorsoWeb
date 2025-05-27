CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100),
    cognome VARCHAR(100),
    telefono VARCHAR(100),
    indirizzo VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100)
);