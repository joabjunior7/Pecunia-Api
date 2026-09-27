package com.fintrack.service;

import com.fintrack.dto.request.TransactionRequest;
import com.fintrack.dto.response.TransactionResponse;
import com.fintrack.entity.Account;
import com.fintrack.entity.Category;
import com.fintrack.entity.Transaction;
import com.fintrack.enums.AccountType;
import com.fintrack.enums.TransactionType;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TransactionService.
 * Valida a consistência de saldo e o fluxo contábil de receitas e despesas.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;
    private Category category;
    private Transaction transaction;
    private TransactionRequest incomeRequest;
    private TransactionRequest expenseRequest;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .name("Nubank")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .active(true)
                .build();

        category = Category.builder()
                .id(1L)
                .name("Alimentação")
                .active(true)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .description("Supermercado")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .account(account)
                .category(category)
                .build();

        incomeRequest = TransactionRequest.builder()
                .description("Salário")
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .accountId(1L)
                .categoryId(1L)
                .build();

        expenseRequest = TransactionRequest.builder()
                .description("Restaurante")
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .accountId(1L)
                .categoryId(1L)
                .build();
    }

    @Test
    @DisplayName("Deve somar ao saldo da conta ao lançar uma RECEITA (INCOME)")
    void create_QuandoReceita_DeveSomarAoSaldo() {
        // Arrange: saldo inicial é 1000.00, receita é 3000.00
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        // Act
        TransactionResponse result = transactionService.create(incomeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("4000.00"), account.getBalance()); // 1000 + 3000
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve subtrair do saldo da conta ao lançar uma DESPESA (EXPENSE)")
    void create_QuandoDespesa_DeveSubtrairDoSaldo() {
        // Arrange: saldo inicial é 1000.00, despesa é 200.00
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(11L);
            return t;
        });

        // Act
        TransactionResponse result = transactionService.create(expenseRequest);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), account.getBalance()); // 1000 - 200
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando conta não existir")
    void create_QuandoContaNaoExiste_DeveLancarExcecao() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.create(incomeRequest));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando categoria não existir")
    void create_QuandoCategoriaNaoExiste_DeveLancarExcecao() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.create(incomeRequest));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve estornar saldo antigo e recalcular novo saldo ao atualizar despesa")
    void update_QuandoAlterarValorDeDespesa_DeveRecalcularSaldo() {
        // Arrange: saldo atual é 1000.00. Transação anterior era despesa de 150.00.
        // Novo request é despesa de 250.00.
        // Saldo deve: estornar os 150 (1000 + 150 = 1150) e aplicar os 250 (1150 - 250 = 900.00)
        when(transactionRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionRequest updateReq = TransactionRequest.builder()
                .description("Supermercado Extra")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .accountId(1L)
                .categoryId(1L)
                .build();

        // Act
        TransactionResponse result = transactionService.update(1L, updateReq);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), account.getBalance());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    @DisplayName("Deve estornar o valor no saldo da conta ao excluir uma despesa")
    void delete_QuandoExcluirDespesa_DeveEstornarAoSaldo() {
        // Arrange: saldo é 1000.00, transação é despesa de 150.00
        when(transactionRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(transaction));

        // Act
        transactionService.delete(1L);

        // Assert: saldo deve voltar para 1000 + 150 = 1150.00
        assertEquals(new BigDecimal("1150.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    @DisplayName("Deve subtrair o valor do saldo da conta ao excluir uma receita")
    void delete_QuandoExcluirReceita_DeveSubtrairDoSaldo() {
        // Arrange: transação de receita de 500.00
        Transaction incomeTx = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME)
                .account(account)
                .build();

        when(transactionRepository.findByIdWithAssociations(2L)).thenReturn(Optional.of(incomeTx));

        // Act
        transactionService.delete(2L);

        // Assert: saldo era 1000.00, ao deletar a receita, volta para 1000 - 500 = 500.00
        assertEquals(new BigDecimal("500.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).delete(incomeTx);
    }

    @Test
    @DisplayName("Deve listar transações com paginação")
    void findAll_DeveRetornarPaginaDeTransacoes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findAllWithAssociations(pageable)).thenReturn(page);

        // Act
        Page<TransactionResponse> result = transactionService.findAll(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Supermercado", result.getContent().get(0).getDescription());
    }
}
