package com.fintrack.service;

import com.fintrack.dto.projection.CategoryTotalProjection;
import com.fintrack.dto.response.CategoryExpenseResponse;
import com.fintrack.dto.response.MonthlyEvolutionResponse;
import com.fintrack.dto.response.MonthlySummaryResponse;
import com.fintrack.enums.TransactionType;
import com.fintrack.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço responsável por gerar os dados analíticos e relatórios para dashboards.
 *
 * --- BOAS PRÁTICAS FINANCEIRAS ---
 * 1. Todos os cálculos de porcentagem utilizam BigDecimal com RoundingMode.HALF_UP
 *    (arredondamento bancário padrão: 5 ou mais arredonda para cima).
 * 2. As consultas são otimizadas via agregação direta no banco de dados (SUM/COUNT/GROUP BY),
 *    evitando trazer milhares de registros para a memória da aplicação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    /**
     * Calcula o resumo financeiro do mês (receitas, despesas, saldo líquido e total de transações).
     */
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(int year, int month) {
        log.info("Calculando resumo mensal para {}/{}", month, year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.EXPENSE, startDate, endDate);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        long transactionCount = transactionRepository.countByDateBetween(startDate, endDate);

        return MonthlySummaryResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .transactionCount(transactionCount)
                .build();
    }

    /**
     * Agrupa os gastos do mês por categoria e calcula o percentual de cada uma sobre o total.
     */
    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getExpensesByCategory(int year, int month) {
        log.info("Calculando despesas por categoria para {}/{}", month, year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();

        BigDecimal totalExpense = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.EXPENSE, startDate, endDate);

        List<CategoryTotalProjection> projections = transactionRepository
                .findCategoryTotalsByTypeAndDateBetween(TransactionType.EXPENSE, startDate, endDate);

        List<CategoryExpenseResponse> responses = new ArrayList<>();

        for (CategoryTotalProjection p : projections) {
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                percentage = p.getTotalAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(totalExpense, 2, RoundingMode.HALF_UP);
            }

            responses.add(CategoryExpenseResponse.builder()
                    .categoryId(p.getCategoryId())
                    .categoryName(p.getCategoryName())
                    .totalAmount(p.getTotalAmount())
                    .percentage(percentage)
                    .transactionCount(p.getTransactionCount() != null ? p.getTransactionCount() : 0L)
                    .build());
        }

        return responses;
    }

    /**
     * Retorna a evolução mensal de receitas, despesas e saldo dos 12 meses de um ano.
     */
    @Transactional(readOnly = true)
    public List<MonthlyEvolutionResponse> getYearlyEvolution(int year) {
        log.info("Calculando evolução anual para o ano {}", year);

        List<MonthlyEvolutionResponse> evolutionList = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            LocalDate startDate = LocalDate.of(year, m, 1);
            LocalDate endDate = YearMonth.of(year, m).atEndOfMonth();

            BigDecimal income = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.INCOME, startDate, endDate);
            BigDecimal expense = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.EXPENSE, startDate, endDate);
            BigDecimal balance = income.subtract(expense);

            String monthName = Month.of(m).getDisplayName(TextStyle.FULL, LOCALE_BR);
            // Capitaliza o nome do mês (ex: "Janeiro")
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

            evolutionList.add(MonthlyEvolutionResponse.builder()
                    .year(year)
                    .month(m)
                    .monthName(monthName)
                    .income(income)
                    .expense(expense)
                    .balance(balance)
                    .build());
        }

        return evolutionList;
    }
}
