package com.fintrack.repository;

import com.fintrack.entity.Transaction;
import com.fintrack.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade Transaction.
 *
 * --- PONTO DE ENTREVISTA: O PROBLEMA DO N+1 E O JOIN FETCH ---
 * Como marcamos os relacionamentos com Account e Category como LAZY na entidade,
 * se fizermos um simples `findAll()`, o JPA faria 1 query para buscar as transações
 * e depois +1 query para cada conta e categoria acessada (N+1 queries).
 *
 * Para carregar as transações já com a Conta e a Categoria em UMA ÚNICA QUERY
 * eficiente, usamos `JOIN FETCH` na JPQL!
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Busca todas as transações com paginação, carregando Account e Category com JOIN FETCH
     * para evitar N+1 queries. O countQuery separado é obrigatório no Spring Data JPA
     * ao usar JOIN FETCH com Pageable.
     */
    @Query(value = "SELECT t FROM Transaction t JOIN FETCH t.account JOIN FETCH t.category",
           countQuery = "SELECT count(t) FROM Transaction t")
    Page<Transaction> findAllWithAssociations(Pageable pageable);

    /**
     * Busca uma transação por ID já trazendo Conta e Categoria carregadas.
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.account JOIN FETCH t.category WHERE t.id = :id")
    Optional<Transaction> findByIdWithAssociations(@Param("id") Long id);

    /**
     * Filtra transações por período (data inicial e data final) com paginação.
     */
    @Query(value = "SELECT t FROM Transaction t JOIN FETCH t.account JOIN FETCH t.category " +
                   "WHERE t.date BETWEEN :startDate AND :endDate",
           countQuery = "SELECT count(t) FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate")
    Page<Transaction> findByDateBetween(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);

    /**
     * Filtra transações por conta específica com paginação.
     */
    @Query(value = "SELECT t FROM Transaction t JOIN FETCH t.account JOIN FETCH t.category " +
                   "WHERE t.account.id = :accountId",
           countQuery = "SELECT count(t) FROM Transaction t WHERE t.account.id = :accountId")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Soma o valor total das transações por tipo em determinado período.
     * Útil para o cálculo rápido de totais de receitas e despesas.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateBetween(@Param("type") TransactionType type,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
