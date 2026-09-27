package com.pecunia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Resumo financeiro de um mês específico:
 * - Total de receitas
 * - Total de despesas
 * - Saldo líquido do período (receitas - despesas)
 * - Quantidade de transações
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private long transactionCount;
}
