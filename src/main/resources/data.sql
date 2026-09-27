-- Categorias padrao para o ambiente de desenvolvimento (H2)
-- Em producao, o Flyway cuida disso via migration

-- Limpa e reinsere para garantir dados consistentes a cada restart
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
DELETE FROM accounts;

INSERT INTO accounts (name, type, balance, description, active, created_at) VALUES
    ('Nubank',          'BANK',        1500.00, 'Conta corrente principal', TRUE, CURRENT_TIMESTAMP),
    ('Carteira Física', 'WALLET',       120.50, 'Dinheiro em espécie',       TRUE, CURRENT_TIMESTAMP),
    ('Cartão Inter',    'CREDIT_CARD',    0.00, 'Cartão de crédito Inter',    TRUE, CURRENT_TIMESTAMP);

