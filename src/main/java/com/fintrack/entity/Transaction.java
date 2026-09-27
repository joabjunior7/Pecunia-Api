package com.fintrack.entity;

import com.fintrack.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma transação financeira (Receita ou Despesa).
 *
 * É o núcleo do FinTrack: cada transação pertence a uma Conta (Account)
 * e a uma Categoria (Category).
 *
 * --- PONTOS IMPORTANTES DE ENTREVISTA ---
 *
 * 1. FetchType.LAZY:
 *    Por padrão, @ManyToOne usa FetchType.EAGER (carrega a entidade relacionada
 *    imediatamente com JOIN). Mudamos para LAZY para evitar o clássico problema do N+1
 *    e carregar a conta/categoria somente quando estritamente necessário.
 *
 * 2. @ToString.Exclude:
 *    Evita que o toString() do Lombok acesse campos LAZY fora de uma transação aberta
 *    (o que causaria LazyInitializationException) ou crie loops recursivos.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"account", "category"})
@EqualsAndHashCode(of = "id")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
