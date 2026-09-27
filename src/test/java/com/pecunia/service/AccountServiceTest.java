package com.pecunia.service;

import com.pecunia.dto.request.AccountRequest;
import com.pecunia.dto.response.AccountResponse;
import com.pecunia.entity.Account;
import com.pecunia.enums.AccountType;
import com.pecunia.exception.BusinessException;
import com.pecunia.exception.ResourceNotFoundException;
import com.pecunia.repository.AccountRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AccountService.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountRequest request;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .name("Nubank")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1500.00"))
                .description("Conta principal")
                .active(true)
                .build();

        request = AccountRequest.builder()
                .name("Nubank")
                .type(AccountType.BANK)
                .initialBalance(new BigDecimal("1500.00"))
                .description("Conta principal")
                .build();
    }

    @Test
    @DisplayName("Deve listar contas ativas com paginação")
    void findAll_DeveRetornarPaginaDeContas() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(account));
        when(accountRepository.findByActiveTrue(pageable)).thenReturn(page);

        // Act
        Page<AccountResponse> result = accountService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Nubank", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Deve retornar conta quando ID existir")
    void findById_QuandoExiste_DeveRetornarConta() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));

        // Act
        AccountResponse result = accountService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nubank", result.getName());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando conta não existir")
    void findById_QuandoNaoExiste_DeveLancarExcecao() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> accountService.findById(99L));
    }

    @Test
    @DisplayName("Deve criar conta com saldo inicial informado")
    void create_QuandoDadosValidos_DeveCriarComSaldo() {
        // Arrange
        when(accountRepository.existsByNameIgnoreCase("Nubank")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        AccountResponse result = accountService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("Nubank", result.getName());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve criar conta com saldo zero se saldo inicial for nulo")
    void create_QuandoSaldoInicialNulo_DeveIniciarComZero() {
        // Arrange
        request.setInitialBalance(null);
        when(accountRepository.existsByNameIgnoreCase("Nubank")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(2L);
            return acc;
        });

        // Act
        AccountResponse result = accountService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao criar conta com nome duplicado")
    void create_QuandoNomeDuplicado_DeveLancarBusinessException() {
        // Arrange
        when(accountRepository.existsByNameIgnoreCase("Nubank")).thenReturn(true);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> accountService.create(request));
        assertTrue(ex.getMessage().contains("Já existe uma conta"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar dados da conta sem alterar o saldo")
    void update_QuandoValido_NaoDeveAlterarSaldo() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(accountRepository.existsByNameIgnoreCaseAndIdNot("Nubank PJ", 1L)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountRequest updateRequest = AccountRequest.builder()
                .name("Nubank PJ")
                .type(AccountType.BANK)
                .initialBalance(new BigDecimal("9999.00")) // Tentando mudar o saldo
                .description("Descrição nova")
                .build();

        // Act
        AccountResponse result = accountService.update(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), account.getBalance()); // Saldo permanece inalterado!
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Deve desativar conta (soft delete)")
    void delete_QuandoValido_DeveDesativar() {
        // Arrange
        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));

        // Act
        accountService.delete(1L);

        // Assert
        assertFalse(account.isActive());
        verify(accountRepository, times(1)).save(account);
    }
}
