package com.pecunia.controller;

import com.pecunia.dto.request.AccountRequest;
import com.pecunia.dto.response.ApiResponse;
import com.pecunia.dto.response.AccountResponse;
import com.pecunia.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gerenciamento de contas.
 *
 * GET    /api/accounts            -> Lista contas com paginação (?page=0&size=10&sort=name,asc)
 * GET    /api/accounts/{id}       -> Detalha uma conta por ID
 * POST   /api/accounts            -> Cadastra nova conta (201 Created)
 * PUT    /api/accounts/{id}       -> Atualiza conta existente
 * DELETE /api/accounts/{id}       -> Desativa (soft delete) conta (204 No Content)
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Lista contas com paginação.
     * Exemplo de chamada: GET /api/accounts?page=0&size=5&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<AccountResponse> page = accountService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * Busca os dados de uma conta específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> findById(@PathVariable Long id) {
        AccountResponse account = accountService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * Cadastra uma nova conta.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @Valid @RequestBody AccountRequest request) {

        AccountResponse created = accountService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conta criada com sucesso", created));
    }

    /**
     * Atualiza dados de uma conta existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {

        AccountResponse updated = accountService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conta atualizada com sucesso", updated));
    }

    /**
     * Desativa uma conta por soft delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
