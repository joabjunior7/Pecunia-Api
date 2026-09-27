package com.fintrack.dto.request;

import com.fintrack.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para criação e atualização de transações financeiras.
 *
 * --- PONTO DE DESIGN ---
 * Recebemos apenas os IDs de Account e Category, em vez de objetos inteiros.
 * Isso desacopla o contrato da API das tabelas do banco e facilita para
 * os clientes que consomem a API REST (React, mobile, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "A descrição da transação é obrigatória")
    @Size(min = 2, max = 100, message = "A descrição deve ter entre 2 e 100 caracteres")
    private String description;

    @NotNull(message = "O valor da transação é obrigatório")
    @Positive(message = "O valor da transação deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "A data da transação é obrigatória")
    private LocalDate date;

    @NotNull(message = "O tipo da transação é obrigatório (INCOME ou EXPENSE)")
    private TransactionType type;

    @NotNull(message = "O ID da conta é obrigatório")
    private Long accountId;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;

    @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
    private String notes;
}
