package com.pecunia.controller;

import com.pecunia.dto.request.CategoryRequest;
import com.pecunia.dto.response.ApiResponse;
import com.pecunia.dto.response.CategoryResponse;
import com.pecunia.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST de Category.
 *
 * --- ENDPOINTS ---
 *
 * GET    /api/categories      -> Lista todas as categorias
 * GET    /api/categories/{id} -> Busca uma categoria por ID
 * POST   /api/categories      -> Cria uma nova categoria
 * PUT    /api/categories/{id} -> Atualiza uma categoria
 * DELETE /api/categories/{id} -> Exclui (desativa) uma categoria
 *
 * --- CONCEITOS ---
 *
 * @RestController -> Combina @Controller + @ResponseBody.
 *                    Todos os metodos retornam JSON automaticamente.
 *
 * @RequestMapping -> Define o caminho base. Todos os endpoints
 *                    deste controller comecam com /api/categories.
 *
 * @Valid -> Ativa a validacao do DTO. Se algum campo for invalido,
 *           o Spring lanca MethodArgumentNotValidException, que o
 *           GlobalExceptionHandler trata e retorna uma resposta formatada.
 *
 * ResponseEntity -> Permite definir o status HTTP da resposta.
 *                   Ex: 201 CREATED ao criar, 204 NO_CONTENT ao excluir.
 *
 * Pergunta de entrevista: "Qual a diferenca entre @Controller e @RestController?"
 * Resposta: "@Controller retorna views (HTML). @RestController retorna dados (JSON/XML)
 * direto no corpo da resposta, equivale a @Controller + @ResponseBody."
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Lista todas as categorias ativas.
     *
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAll() {
        List<CategoryResponse> categories = categoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * GET /api/categories/{id}
     * Busca uma categoria pelo ID.
     *
     * Status: 200 OK ou 404 NOT_FOUND
     *
     * @PathVariable -> Extrai o {id} da URL.
     *                  Ex: GET /api/categories/5 -> id = 5
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable Long id) {
        CategoryResponse category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * POST /api/categories
     * Cria uma nova categoria.
     *
     * Status: 201 CREATED (nao 200! - boa pratica REST)
     *
     * @RequestBody -> Converte o JSON do corpo da requisicao para o DTO
     * @Valid       -> Valida os campos antes de processar
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse created = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Categoria criada com sucesso", created));
    }

    /**
     * PUT /api/categories/{id}
     * Atualiza uma categoria existente.
     *
     * Status: 200 OK, 404 NOT_FOUND ou 422 UNPROCESSABLE_ENTITY
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse updated = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Categoria atualizada com sucesso", updated));
    }

    /**
     * DELETE /api/categories/{id}
     * Desativa (soft delete) uma categoria.
     *
     * Status: 204 NO_CONTENT (sem corpo na resposta - padrao REST para delete)
     *
     * Pergunta de entrevista: "Qual status HTTP voce usa para DELETE?"
     * Resposta: "204 No Content quando nao retorno dados, ou 200 OK
     * se retorno uma confirmacao no corpo."
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
