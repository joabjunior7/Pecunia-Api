package com.fintrack.service;

import com.fintrack.dto.projection.CategoryTotalProjection;
import com.fintrack.dto.response.CategoryExpenseResponse;
import com.fintrack.dto.response.MonthlyEvolutionResponse;
import com.fintrack.dto.response.MonthlySummaryResponse;
import com.fintrack.enums.TransactionType;
import com.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o DashboardService.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve calcular resumo mensal com saldo líquido correto")
    void getMonthlySummary_DeveCalcularSaldoLiquido() {
        // Arrange: receitas de 5000, despesas de 2000 -> saldo líquido de 3000
        when(transactionRepository.sumByTypeAndDateBetween(eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumByTypeAndDateBetween(eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.countByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(15L);

        // Act
        MonthlySummaryResponse result = dashboardService.getMonthlySummary(2026, 9);

        // Assert
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(9, result.getMonth());
        assertEquals(new BigDecimal("5000.00"), result.getTotalIncome());
        assertEquals(new BigDecimal("2000.00"), result.getTotalExpense());
        assertEquals(new BigDecimal("3000.00"), result.getNetBalance()); // 5000 - 2000
        assertEquals(15L, result.getTransactionCount());
    }

    @Test
    @DisplayName("Deve calcular gastos por categoria e percentual proporcional")
    void getExpensesByCategory_DeveCalcularPercentual() {
        // Arrange: Total de despesas = 1000.00
        // Categoria 1 (Alimentação) = 600.00 (60.00%)
        // Categoria 2 (Transporte)  = 400.00 (40.00%)
        when(transactionRepository.sumByTypeAndDateBetween(eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        CategoryTotalProjection proj1 = mock(CategoryTotalProjection.class);
        when(proj1.getCategoryId()).thenReturn(1L);
        when(proj1.getCategoryName()).thenReturn("Alimentação");
        when(proj1.getTotalAmount()).thenReturn(new BigDecimal("600.00"));
        when(proj1.getTransactionCount()).thenReturn(3L);

        CategoryTotalProjection proj2 = mock(CategoryTotalProjection.class);
        when(proj2.getCategoryId()).thenReturn(2L);
        when(proj2.getCategoryName()).thenReturn("Transporte");
        when(proj2.getTotalAmount()).thenReturn(new BigDecimal("400.00"));
        when(proj2.getTransactionCount()).thenReturn(2L);

        when(transactionRepository.findCategoryTotalsByTypeAndDateBetween(eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(proj1, proj2));

        // Act
        List<CategoryExpenseResponse> result = dashboardService.getExpensesByCategory(2026, 9);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        CategoryExpenseResponse cat1 = result.get(0);
        assertEquals("Alimentação", cat1.getCategoryName());
        assertEquals(new BigDecimal("600.00"), cat1.getTotalAmount());
        assertEquals(new BigDecimal("60.00"), cat1.getPercentage());

        CategoryExpenseResponse cat2 = result.get(1);
        assertEquals("Transporte", cat2.getCategoryName());
        assertEquals(new BigDecimal("400.00"), cat2.getTotalAmount());
        assertEquals(new BigDecimal("40.00"), cat2.getPercentage());
    }

    @Test
    @DisplayName("Deve retornar evolução anual para os 12 meses do ano")
    void getYearlyEvolution_DeveRetornar12Meses() {
        // Arrange
        when(transactionRepository.sumByTypeAndDateBetween(eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4000.00"));
        when(transactionRepository.sumByTypeAndDateBetween(eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2500.00"));

        // Act
        List<MonthlyEvolutionResponse> result = dashboardService.getYearlyEvolution(2026);

        // Assert
        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(1, result.get(0).getMonth());
        assertEquals("Janeiro", result.get(0).getMonthName());
        assertEquals(new BigDecimal("1500.00"), result.get(0).getBalance()); // 4000 - 2500
        assertEquals(12, result.get(11).getMonth());
        assertEquals("Dezembro", result.get(11).getMonthName());
    }
}
