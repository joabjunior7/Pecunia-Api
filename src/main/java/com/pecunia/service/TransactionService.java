package com.pecunia.service;

import com.pecunia.dto.request.TransactionRequest;
import com.pecunia.dto.response.TransactionResponse;
import com.pecunia.entity.Account;
import com.pecunia.entity.Category;
import com.pecunia.entity.Transaction;
import com.pecunia.enums.TransactionType;
import com.pecunia.exception.ResourceNotFoundException;
import com.pecunia.repository.AccountRepository;
import com.pecunia.repository.CategoryRepository;
import com.pecunia.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Camada de serviço que contém as regras de negócio para transações financeiras.
 *
 * --- CONCEITO CHAVE DE ENTREVISTA: TRANSAÇÕES ATÔMICAS COM @Transactional ---
 * Registrar uma movimentação financeira envolve DOIS passos no banco:
 * 1. Salvar a transação na tabela 'transactions'
 * 2. Atualizar o saldo da conta na tabela 'accounts'
 *
 * Se o passo 1 funcionar mas o passo 2 falhar (ou vice-versa), teríamos dados inconsistentes!
 * Com a anotação @Transactional do Spring, garantimos o princípio ACID (Atomicidade):
 * ou TUDO é gravado com sucesso (commit), ou se algo falhar, TUDO é desfeito (rollback).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lista transações com filtros opcionais (período ou conta) e paginação.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(LocalDate startDate, LocalDate endDate, Long accountId, Pageable pageable) {
        log.info("Listando transações - startDate: {}, endDate: {}, accountId: {}, página: {}",
                startDate, endDate, accountId, pageable.getPageNumber());

        Page<Transaction> page;

        if (startDate != null && endDate != null) {
            page = transactionRepository.findByDateBetween(startDate, endDate, pageable);
        } else if (accountId != null) {
            page = transactionRepository.findByAccountId(accountId, pageable);
        } else {
            page = transactionRepository.findAllWithAssociations(pageable);
        }

        return page.map(TransactionResponse::fromEntity);
    }

    /**
     * Busca uma transação por ID.
     */
    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        log.info("Buscando transação ID: {}", id);
        Transaction transaction = transactionRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * Registra uma nova transação e atualiza o saldo da conta de forma atômica.
     */
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        log.info("Criando transação: '{}' de valor R$ {} na conta ID {}",
                request.getDescription(), request.getAmount(), request.getAccountId());

        Account account = findActiveAccount(request.getAccountId());
        Category category = findActiveCategory(request.getCategoryId());

        // Atualiza o saldo da conta conforme o tipo da transação
        applyBalanceImpact(account, request.getType(), request.getAmount());
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .description(request.getDescription().trim())
                .amount(request.getAmount())
                .date(request.getDate())
                .type(request.getType())
                .account(account)
                .category(category)
                .notes(request.getNotes())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transação ID {} criada com sucesso. Novo saldo da conta {}: R$ {}",
                saved.getId(), account.getName(), account.getBalance());

        return TransactionResponse.fromEntity(saved);
    }

    /**
     * Atualiza uma transação existente.
     * Reverte o impacto financeiro anterior e aplica as novas alterações.
     */
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        log.info("Atualizando transação ID: {}", id);

        Transaction transaction = transactionRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));

        Account currentAccount = transaction.getAccount();
        Account newAccount = findActiveAccount(request.getAccountId());
        Category newCategory = findActiveCategory(request.getCategoryId());

        // 1. Reverte o impacto no saldo da conta anterior
        revertBalanceImpact(currentAccount, transaction.getType(), transaction.getAmount());

        // 2. Aplica o novo impacto no saldo da nova conta (que pode ser a mesma ou outra)
        if (currentAccount.getId().equals(newAccount.getId())) {
            applyBalanceImpact(currentAccount, request.getType(), request.getAmount());
            accountRepository.save(currentAccount);
        } else {
            accountRepository.save(currentAccount);
            applyBalanceImpact(newAccount, request.getType(), request.getAmount());
            accountRepository.save(newAccount);
        }

        // 3. Atualiza os dados da transação
        transaction.setDescription(request.getDescription().trim());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setType(request.getType());
        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);
        transaction.setNotes(request.getNotes());

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transação ID {} atualizada com sucesso", updated.getId());

        return TransactionResponse.fromEntity(updated);
    }

    /**
     * Exclui uma transação e estorna o saldo na conta correspondente.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Excluindo transação ID: {}", id);

        Transaction transaction = transactionRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));

        Account account = transaction.getAccount();

        // Estorna o valor do saldo da conta
        revertBalanceImpact(account, transaction.getType(), transaction.getAmount());
        accountRepository.save(account);

        transactionRepository.delete(transaction);
        log.info("Transação ID {} excluída com sucesso. Saldo estornado para a conta {}: R$ {}",
                id, account.getName(), account.getBalance());
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private Account findActiveAccount(Long accountId) {
        return accountRepository.findByIdAndActiveTrue(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", accountId));
    }

    private Category findActiveCategory(Long categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", categoryId));
    }

    /**
     * Aplica o impacto de uma transação no saldo:
     * - RECEITA (INCOME)  -> Saldo = Saldo + Valor
     * - DESPESA (EXPENSE) -> Saldo = Saldo - Valor
     */
    private void applyBalanceImpact(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    /**
     * Reverte o impacto de uma transação no saldo (operação inversa):
     * - RECEITA (INCOME)  -> Saldo = Saldo - Valor
     * - DESPESA (EXPENSE) -> Saldo = Saldo + Valor
     */
    private void revertBalanceImpact(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }
    }
}
