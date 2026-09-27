package com.fintrack.controller;

import com.fintrack.dto.response.ApiResponse;
import com.fintrack.dto.response.CategoryExpenseResponse;
import com.fintrack.dto.response.MonthlyEvolutionResponse;
import com.fintrack.dto.response.MonthlySummaryResponse;
import com.fintrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para o Dashboard e Relatórios Financeiros.
 *
 * GET /api/dashboard/monthly-summary       -> Resumo do mês (receitas, despesas, saldo líquido)
 * GET /api/dashboard/expenses-by-category  -> Distribuição percentual de gastos por categoria
 * GET /api/dashboard/evolution             -> Evolução financeira dos 12 meses do ano
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retorna o resumo financeiro de um mês (total receitas, despesas, saldo líquido).
     * Se ano ou mês não forem informados, assume a data atual.
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySummaryResponse>> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        MonthlySummaryResponse summary = dashboardService.getMonthlySummary(targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * Retorna os gastos acumulados por categoria no mês, com percentual calculado.
     */
    @GetMapping("/expenses-by-category")
    public ResponseEntity<ApiResponse<List<CategoryExpenseResponse>>> getExpensesByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        List<CategoryExpenseResponse> expenses = dashboardService.getExpensesByCategory(targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    /**
     * Retorna a evolução mensal de receitas, despesas e saldo dos 12 meses de um ano.
     */
    @GetMapping("/evolution")
    public ResponseEntity<ApiResponse<List<MonthlyEvolutionResponse>>> getYearlyEvolution(
            @RequestParam(required = false) Integer year) {

        int targetYear = year != null ? year : LocalDate.now().getYear();
        List<MonthlyEvolutionResponse> evolution = dashboardService.getYearlyEvolution(targetYear);
        return ResponseEntity.ok(ApiResponse.success(evolution));
    }
}
