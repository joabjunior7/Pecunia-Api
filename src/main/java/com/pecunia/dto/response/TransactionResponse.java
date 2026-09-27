package com.pecunia.dto.response;

import com.pecunia.entity.Transaction;
import com.pecunia.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO que representa a resposta de uma transação para o cliente.
 *
 * Entrega dados "achatados" (flat), incluindo os nomes da conta e categoria,
 * evitando que o front-end precise fazer requisições adicionais para saber o nome.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private TransactionType type;
    private String typeDescription;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * Converte a entidade Transaction no DTO de resposta.
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .type(transaction.getType())
                .typeDescription(transaction.getType().getDescription())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getName() : null)
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
