-- ================================================================
-- FinTrack API - V2: Criação da tabela de contas (accounts)
-- ================================================================

CREATE TABLE accounts (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50)    NOT NULL,
    type        VARCHAR(20)    NOT NULL,
    balance     NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    description VARCHAR(200),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

-- Índice único case-insensitive para não permitir contas com mesmo nome
CREATE UNIQUE INDEX idx_accounts_name ON accounts (LOWER(name));
