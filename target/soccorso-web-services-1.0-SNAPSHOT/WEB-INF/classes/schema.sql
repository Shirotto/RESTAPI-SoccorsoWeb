CREATE DATABASE IF NOT EXISTS sviluppo_web;

USE sviluppo_web;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100),
    cognome VARCHAR(100),
    telefono VARCHAR(50),
    indirizzo VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('UTENTE', 'ADMIN') DEFAULT 'UTENTE' NOT NULL
);

CREATE TABLE IF NOT EXISTS richieste_soccorso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    users_id BIGINT,
    richiedente VARCHAR(250),
    descrizione VARCHAR(250) NOT NULL,
    indirizzo VARCHAR(255) NOT NULL,
    telefono_contatto_richiesta VARCHAR(50),
    email_contatto_richiesta VARCHAR(100),
    
    stato_richiesta ENUM(
        'PENDING_VALIDATION', -- Inserita, in attesa di convalida tramite
        'ATTIVA',             -- Convalidata, pronta per essere gestita
        'IN_CORSO',           -- Assegnata a missione
        'CHIUSA',             -- Richiesta completata/risolta
        'IGNORATA'            -- Annullata
    ) DEFAULT 'PENDING_VALIDATION' NOT NULL,
    
    livello_successo VARCHAR(50), 
    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   
    validation_token VARCHAR(255) UNIQUE NULL,
    FOREIGN KEY (users_id) REFERENCES users(id)
);

INSERT INTO users (nome, cognome, telefono, indirizzo, email, password, role) 
VALUES ('Admin', 'Sistema', '1234567890', 'Sede Centrale', 'admin@soccorsoweb.it', SHA2('admin123', 256), 'ADMIN');