package com.pecunia.repository;

import com.pecunia.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de banco na entidade Account.
 * Suporta paginação nativa através da interface Pageable do Spring Data.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Retorna uma página de contas ativas.
     * O Spring Data gera tanto a consulta paginada (LIMIT / OFFSET)
     * quanto o COUNT total de registros automaticamente.
     */
    Page<Account> findByActiveTrue(Pageable pageable);

    /**
     * Busca uma conta ativa pelo ID.
     */
    Optional<Account> findByIdAndActiveTrue(Long id);

    /**
     * Verifica duplicata por nome ignorando maiúsculas/minúsculas.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica duplicata ignorando a própria conta durante edição.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
