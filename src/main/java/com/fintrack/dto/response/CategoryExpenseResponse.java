package com.fintrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa os gastos acumulados de uma categoria específica no período,
 * incluindo a porcentagem que ela representa no orçamento total de despesas.
 * Ideal para alimentar gráficos de pizza (donut charts) no front-end.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseResponse {

    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
    private BigDecimal percentage;
    private long transactionCount;
}
