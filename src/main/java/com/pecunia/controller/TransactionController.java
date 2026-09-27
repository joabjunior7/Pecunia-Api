package com.pecunia.controller;

import com.pecunia.dto.request.TransactionRequest;
import com.pecunia.dto.response.ApiResponse;
import com.pecunia.dto.response.TransactionResponse;
import com.pecunia.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller REST para gerenciamento de transações financeiras.
 *
 * GET    /api/transactions            -> Lista transações com filtros e paginação
 * GET    /api/transactions/{id}       -> Detalhes de uma transação específica
 * POST   /api/transactions            -> Lança nova movimentação (201 Created)
 * PUT    /api/transactions/{id}       -> Atualiza transação existente
 * DELETE /api/transactions/{id}       -> Exclui movimentação e estorna saldo (204 No Content)
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Lista transações com suporte a filtros e paginação.
     * Ordenação padrão: por data decrescente (transações mais recentes primeiro).
     *
     * Exemplos:
     * - GET /api/transactions
     * - GET /api/transactions?startDate=2026-09-01&endDate=2026-09-30
     * - GET /api/transactions?accountId=1&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long accountId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TransactionResponse> page = transactionService.findAll(startDate, endDate, accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * Busca uma transação por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(@PathVariable Long id) {
        TransactionResponse transaction = transactionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    /**
     * Lança uma nova transação financeira (Receita ou Despesa).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse created = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Atualiza uma transação existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse updated = transactionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Exclui uma transação e reverte o saldo na conta correspondente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
