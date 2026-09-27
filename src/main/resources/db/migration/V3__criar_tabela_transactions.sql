-- ================================================================
-- FinTrack API - V3: Criação da tabela de transações (transactions)
-- ================================================================

CREATE TABLE transactions (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description      VARCHAR(100)   NOT NULL,
    amount           NUMERIC(15, 2) NOT NULL,
    transaction_date DATE           NOT NULL,
    type             VARCHAR(20)    NOT NULL,
    account_id       BIGINT         NOT NULL REFERENCES accounts(id),
    category_id      BIGINT         NOT NULL REFERENCES categories(id),
    notes            VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimizar filtros comuns:
-- 1. Buscar transações por conta
CREATE INDEX idx_transactions_account_id ON transactions (account_id);

-- 2. Buscar transações por categoria
CREATE INDEX idx_transactions_category_id ON transactions (category_id);

-- 3. Buscar transações por intervalo de datas (extrato / relatórios)
CREATE INDEX idx_transactions_date ON transactions (transaction_date);
