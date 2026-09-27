-- Categorias padrao para o ambiente de desenvolvimento (H2)
-- Em producao, o Flyway cuida disso via migration

-- Limpa dados em ordem para respeitar chaves estrangeiras
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM categories;

INSERT INTO categories (name, description, is_default, active, created_at) VALUES
    ('Alimentacao',   'Refeicoes, supermercado, delivery',     TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Transporte',    'Combustivel, transporte publico, Uber', TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Moradia',       'Aluguel, condominio, contas da casa',   TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Saude',         'Plano de saude, farmacia, consultas',   TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Educacao',      'Cursos, livros, materiais',             TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Lazer',         'Cinema, jogos, viagens',                TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Salario',       'Salario e rendimentos fixos',           TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Freelance',     'Trabalhos avulsos e projetos extras',   TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Investimentos', 'Rendimentos de investimentos',          TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Outros',        'Transacoes sem categoria especifica',   TRUE, TRUE, CURRENT_TIMESTAMP);

-- Contas iniciais de teste
INSERT INTO accounts (name, type, balance, description, active, created_at) VALUES
    ('Nubank',          'BANK',        1500.00, 'Conta corrente principal', TRUE, CURRENT_TIMESTAMP),
    ('Carteira Física', 'WALLET',       120.50, 'Dinheiro em espécie',       TRUE, CURRENT_TIMESTAMP),
    ('Cartão Inter',    'CREDIT_CARD',    0.00, 'Cartão de crédito Inter',    TRUE, CURRENT_TIMESTAMP);

-- Transações iniciais de teste
INSERT INTO transactions (description, amount, transaction_date, type, account_id, category_id, notes, created_at) VALUES
    ('Salário Mensal',          4500.00, '2026-09-05', 'INCOME',  1, 7, 'Salário referente ao mês', CURRENT_TIMESTAMP),
    ('Supermercado Pão Açúcar',  350.20, '2026-09-08', 'EXPENSE', 1, 1, 'Compras da semana',         CURRENT_TIMESTAMP),
    ('Almoço Restaurante',        45.00, '2026-09-10', 'EXPENSE', 2, 1, 'Almoço no centro',          CURRENT_TIMESTAMP),
    ('Gasolina Posto Shell',     180.00, '2026-09-12', 'EXPENSE', 1, 2, 'Abastecimento',             CURRENT_TIMESTAMP),
    ('Projeto Freelance Web',    800.00, '2026-09-15', 'INCOME',  1, 8, 'Primeira parcela landing',  CURRENT_TIMESTAMP);

