package com.fintrack.service;

import com.fintrack.dto.request.AccountRequest;
import com.fintrack.dto.response.AccountResponse;
import com.fintrack.entity.Account;
import com.fintrack.exception.BusinessException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service responsável pelas regras de negócio de contas financeiras.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Lista contas ativas com suporte a paginação e ordenação.
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> findAll(Pageable pageable) {
        log.info("Buscando contas ativas - página: {}, tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        return accountRepository.findByActiveTrue(pageable)
                .map(AccountResponse::fromEntity);
    }

    /**
     * Busca uma conta ativa por ID.
     */
    @Transactional(readOnly = true)
    public AccountResponse findById(Long id) {
        log.info("Buscando conta por ID: {}", id);
        Account account = findActiveById(id);
        return AccountResponse.fromEntity(account);
    }

    /**
     * Cadastra uma nova conta bancária ou carteira.
     */
    @Transactional
    public AccountResponse create(AccountRequest request) {
        log.info("Criando conta: {}", request.getName());

        validateUniqueName(request.getName(), null);

        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .name(request.getName().trim())
                .type(request.getType())
                .balance(initialBalance)
                .description(request.getDescription())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Conta criada com sucesso com ID: {}", saved.getId());

        return AccountResponse.fromEntity(saved);
    }

    /**
     * Atualiza dados cadastrais de uma conta existente.
     * Nota: O saldo não é alterado aqui para preservar a integridade contábil.
     */
    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        log.info("Atualizando conta ID: {}", id);

        Account account = findActiveById(id);
        validateUniqueName(request.getName(), id);

        account.setName(request.getName().trim());
        account.setType(request.getType());
        account.setDescription(request.getDescription());

        Account updated = accountRepository.save(account);
        log.info("Conta ID {} atualizada com sucesso", updated.getId());

        return AccountResponse.fromEntity(updated);
    }

    /**
     * Realiza soft delete (desativa a conta).
     */
    @Transactional
    public void delete(Long id) {
        log.info("Desativando conta ID: {}", id);

        Account account = findActiveById(id);
        account.setActive(false);
        accountRepository.save(account);

        log.info("Conta {} (ID: {}) desativada", account.getName(), id);
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private Account findActiveById(Long id) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
    }

    private void validateUniqueName(String name, Long excludeId) {
        boolean exists;
        if (excludeId == null) {
            exists = accountRepository.existsByNameIgnoreCase(name.trim());
        } else {
            exists = accountRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
        }

        if (exists) {
            throw new BusinessException("Já existe uma conta cadastrada com o nome: " + name);
        }
    }
}
