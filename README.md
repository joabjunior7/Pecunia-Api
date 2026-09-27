# Pecunia API

Backend de controle e inteligência financeira pessoal e corporativa, desenvolvido com **Java 21** e **Spring Boot 3**.

O objetivo deste projeto foi construir uma API REST com padrão de mercado, focando em integridade contábil, precisão decimal, performance em banco de dados relacional e cobertura de testes unitários.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21 LTS**
* **Spring Boot 3.5.3** (Spring Data JPA, Spring Web, Validation, Actuator, DevTools)
* **PostgreSQL 16** (Banco de produção)
* **H2 Database** (Banco em memória para desenvolvimento local rápido)
* **Flyway** (Versionamento e migrations do banco de dados)
* **JUnit 5 & Mockito** (Testes unitários)
* **Lombok** (Produtividade e redução de boilerplate)
* **Docker** (Containerização e deploy)

---

## 💡 Decisões Técnicas e Arquitetura

O projeto adota uma arquitetura em camadas bem definida (**Controller → Service → Repository + DTOs**), priorizando boas práticas que evitam problemas clássicos de sistemas financeiros:

### 1. Precisão Monetária com `BigDecimal`
Valores monetários nunca utilizam tipos de ponto flutuante binário (`Double` ou `Float`), pois eles causam imprecisões acumuladas de centavos (devido ao padrão IEEE 754). Todos os cálculos de saldo, totais e percentuais utilizam `BigDecimal` com arredondamento bancário (`RoundingMode.HALF_UP`).

### 2. Controle Atômico de Saldo e Auditoria (`@Transactional`)
Em sistemas contábeis sérios, o saldo de uma conta não deve ser editado manualmente através de um endpoint comum. No Pecunia:
* Ao cadastrar uma conta, você pode definir um saldo inicial.
* Depois de criada, o saldo só é modificado através de movimentações financeiras (**Receitas** somam, **Despesas** subtraem).
* Toda a operação de salvar a transação e atualizar o saldo ocorre dentro de uma transação atômica (`@Transactional`). Se qualquer etapa falhar, o Spring realiza o rollback automático, garantindo o princípio ACID.
* Na exclusão ou edição de transações, o saldo da conta afetada é estornado automaticamente.

### 3. Prevenção do Problema do N+1 e Alta Performance
* Relacionamentos entre Transação, Conta e Categoria utilizam `FetchType.LAZY` para evitar carregar entidades desnecessárias na memória.
* Nas listagens de extrato, utilizamos consultas JPQL customizadas com **`JOIN FETCH`**, trazendo a transação junto com os dados da conta e da categoria em **uma única query SQL otimizada**.
* Para os relatórios agregados do dashboard (`GROUP BY` e `SUM`), usamos **Interface-based Projections** do Spring Data JPA, trazendo direto do banco apenas as colunas necessárias para o gráfico, sem instanciar entidades pesadas.

### 4. Validação na Borda (Fail-Fast) e Exception Handler Global
* Validações de entrada utilizam Jakarta Bean Validation (`@NotBlank`, `@Positive`, `@NotNull`, `@Size`) nos DTOs.
* Se um payload inválido for enviado, o Spring intercepta antes de chegar no Service e retorna um `400 Bad Request` com a lista dos campos inválidos tratado por um `@RestControllerAdvice`.

### 5. Schema-as-Code com Flyway
* Em produção, o Hibernate não altera tabelas (`ddl-auto: validate`). Toda evolução de schema passa por migrations SQL versionadas:
  * `V1__criar_tabela_categories.sql`
  * `V2__criar_tabela_accounts.sql`
  * `V3__criar_tabela_transactions.sql`

---

## 📌 Principais Endpoints

### 📂 Categorias (`/api/categories`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/categories` | Lista todas as categorias ativas |
| `GET` | `/api/categories/{id}` | Busca categoria por ID |
| `POST` | `/api/categories` | Cria nova categoria personalizada |
| `PUT` | `/api/categories/{id}` | Atualiza categoria (categorias padrão do sistema são bloqueadas para edição) |
| `DELETE` | `/api/categories/{id}` | Desativa categoria via Soft Delete |

### 🏦 Contas (`/api/accounts`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/accounts` | Lista contas com paginação (`?page=0&size=10&sort=name,asc`) |
| `GET` | `/api/accounts/{id}` | Detalha uma conta e exibe o saldo atual |
| `POST` | `/api/accounts` | Cria conta (WALLET, BANK ou CREDIT_CARD) com saldo inicial opcional |
| `PUT` | `/api/accounts/{id}` | Atualiza nome e descrição (saldo protegido contra alteração direta) |
| `DELETE` | `/api/accounts/{id}` | Desativação segura (Soft Delete) |

### 💰 Transações (`/api/transactions`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/transactions` | Extrato paginado com filtros opcionais (`?startDate=...&endDate=...&accountId=...`) |
| `GET` | `/api/transactions/{id}` | Detalhes de um lançamento específico |
| `POST` | `/api/transactions` | Lança receita ou despesa e atualiza o saldo da conta em tempo real |
| `PUT` | `/api/transactions/{id}` | Atualiza lançamento com reconciliação automática de saldo |
| `DELETE` | `/api/transactions/{id}` | Exclui movimentação e estorna o saldo na conta de origem |

### 📊 Dashboard e Relatórios (`/api/dashboard`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/dashboard/monthly-summary` | Resumo mensal (total receitas, despesas e saldo líquido do mês) |
| `GET` | `/api/dashboard/expenses-by-category` | Despesas agrupadas por categoria com % calculada do orçamento |
| `GET` | `/api/dashboard/evolution` | Balanço mês a mês dos 12 meses do ano |

*Observação: Os endpoints do dashboard aceitam os parâmetros opcionais `?year=2026&month=9`. Caso não sejam passados, utilizam o mês e ano atuais por padrão.*

---

## 🧪 Testes Unitários

O projeto possui suíte de testes unitários cobrindo as regras de negócio de todos os serviços (`CategoryService`, `AccountService`, `TransactionService` e `DashboardService`), aplicando o padrão **AAA (Arrange, Act, Assert)** com Mockito.

Para rodar todos os testes no terminal:

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

---

## 🚀 Como Rodar o Projeto Localmente

### Pré-requisitos
* Java 21 instalado (JDK 21)
* Git

### Execução

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/joabjunior7/Pecunia-Api.git
   cd Pecunia-Api
   ```

2. **Inicie a aplicação:**
   * No Windows:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   * No Linux/macOS:
     ```bash
     ./mvnw spring-boot:run
     ```

3. **Acessos úteis em ambiente de desenvolvimento:**
   * **API Base:** `http://localhost:8080/api`
   * **Health Check:** `http://localhost:8080/actuator/health`
   * **Console do Banco H2:** `http://localhost:8080/h2-console`
     * *JDBC URL:* `jdbc:h2:mem:pecuniadb`
     * *User:* `sa`
     * *Password:* *(deixe em branco)*

*(Em ambiente `dev`, o H2 inicia em memória e popula automaticamente categorias, contas e transações de teste através do `data.sql`).*

---

## 🗺️ Próximos Passos (Roadmap)

- [x] CRUD de Categorias com proteção de categorias nativas
- [x] CRUD de Contas com paginação e soft delete
- [x] Módulo de Transações com conciliação automática de saldo
- [x] Endpoints de Dashboard e Relatórios analíticos
- [x] Cobertura de testes unitários com JUnit 5 e Mockito
- [ ] Deploy da API em produção com Docker e Coolify na VPS
- [ ] Desenvolvimento do frontend web responsivo (**Pecunia App**)
- [ ] Autenticação de usuários com Spring Security e JWT

---

## 👨‍💻 Autor

Desenvolvido por **Joab Junior**.  
GitHub: [@joabjunior7](https://github.com/joabjunior7)
