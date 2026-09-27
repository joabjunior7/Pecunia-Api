-- ================================================================
-- Pecunia API - V1: Criacao da tabela de categorias
-- ================================================================
-- Flyway e uma ferramenta de migrations (versionamento do banco).
-- Cada arquivo V{numero}__{descricao}.sql e executado uma vez
-- e na ordem numerica.
--
-- POR QUE USAR FLYWAY?
-- Sem Flyway, voce teria que entrar no banco e criar tabelas na mao.
-- Com Flyway, o esquema do banco fica versionado junto com o codigo.
-- Quando fizer deploy na VPS, o Flyway cria tudo automaticamente.
--
-- Pergunta de entrevista: "Como voce gerencia o schema do banco?"
-- Resposta: "Uso Flyway para migrations versionadas. Cada mudanca
-- no schema e um arquivo SQL versionado que roda automaticamente
-- no startup da aplicacao."
-- ================================================================

CREATE TABLE categories (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

-- Indice unico para evitar categorias duplicadas (case-insensitive)
CREATE UNIQUE INDEX idx_categories_name ON categories (LOWER(name));

-- Categorias padrao do sistema
INSERT INTO categories (name, description, is_default) VALUES
    ('Alimentacao',  'Refeicoes, supermercado, delivery',    TRUE),
    ('Transporte',   'Combustivel, transporte publico, Uber', TRUE),
    ('Moradia',      'Aluguel, condominio, contas da casa',  TRUE),
    ('Saude',        'Plano de saude, farmacia, consultas',  TRUE),
    ('Educacao',     'Cursos, livros, materiais',            TRUE),
    ('Lazer',        'Cinema, jogos, viagens',               TRUE),
    ('Salario',      'Salario e rendimentos fixos',          TRUE),
    ('Freelance',    'Trabalhos avulsos e projetos extras',  TRUE),
    ('Investimentos','Rendimentos de investimentos',         TRUE),
    ('Outros',       'Transacoes sem categoria especifica',  TRUE);
