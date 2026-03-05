CREATE TABLE client (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL,
    birth_date   DATE,
    gender       CHAR(1),
    cpf          VARCHAR(14)  UNIQUE,
    code_country VARCHAR(5),
    cellphone    VARCHAR(20),
    rg           VARCHAR(20),
    name_father  VARCHAR(100),
    name_mother  VARCHAR(100),
    observations TEXT
);

CREATE INDEX idx_client_cpf ON client (cpf);
