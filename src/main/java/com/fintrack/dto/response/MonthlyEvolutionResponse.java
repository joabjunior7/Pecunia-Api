package com.fintrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Evolução financeira mês a mês ao longo do ano.
 * Ideal para gráficos de barras ou linhas mostrando receitas vs despesas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyEvolutionResponse {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;
}
