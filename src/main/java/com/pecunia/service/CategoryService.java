package com.pecunia.service;

import com.pecunia.dto.request.CategoryRequest;
import com.pecunia.dto.response.CategoryResponse;
import com.pecunia.entity.Category;
import com.pecunia.exception.BusinessException;
import com.pecunia.exception.ResourceNotFoundException;
import com.pecunia.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de Category - onde ficam as regras de negocio.
 *
 * --- CONCEITOS IMPORTANTES ---
 *
 * @Service -> Marca a classe como um componente de servico do Spring.
 *             O Spring cria uma unica instancia (singleton) e injeta
 *             onde for necessario.
 *
 * @RequiredArgsConstructor (Lombok) -> Gera um construtor com todos os
 *             campos 'final'. O Spring usa esse construtor para injetar
 *             o repository automaticamente (injecao por construtor).
 *
 *             Pergunta de entrevista: "Como voce faz injecao de dependencia?"
 *             Resposta: "Por construtor, usando @RequiredArgsConstructor do Lombok.
 *             Nao uso @Autowired em campo porque injecao por construtor e a
 *             recomendacao oficial do Spring - facilita testes e garante imutabilidade."
 *
 * @Transactional -> Garante que o metodo roda dentro de uma transacao.
 *             Se der erro, faz rollback automaticamente.
 *             readOnly=true otimiza queries de leitura (nao faz flush).
 *
 * @Slf4j (Lombok) -> Gera automaticamente: private static final Logger log = ...
 *             Usar 'log.info()' em vez de System.out.println()!
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lista todas as categorias ativas.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        log.info("Buscando todas as categorias ativas");

        return categoryRepository.findByActiveTrue()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    /**
     * Busca uma categoria pelo ID.
     * Lanca excecao se nao encontrar (ou se estiver inativa).
     */
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        log.info("Buscando categoria com id: {}", id);

        Category category = findActiveById(id);
        return CategoryResponse.fromEntity(category);
    }

    /**
     * Cria uma nova categoria.
     * Valida se ja existe uma categoria com o mesmo nome.
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        log.info("Criando nova categoria: {}", request.getName());

        // Regra de negocio: nao pode ter duas categorias com o mesmo nome
        validateUniqueName(request.getName(), null);

        // Converte DTO -> Entity
        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        // Salva no banco e converte Entity -> DTO de resposta
        Category saved = categoryRepository.save(category);
        log.info("Categoria criada com id: {}", saved.getId());

        return CategoryResponse.fromEntity(saved);
    }

    /**
     * Atualiza uma categoria existente.
     * Lanca excecao se nao encontrar ou se o novo nome ja existir.
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Atualizando categoria com id: {}", id);

        Category category = findActiveById(id);

        // Categoria padrao nao pode ser editada
        if (category.isDefault()) {
            throw new BusinessException("Categorias padrao nao podem ser editadas");
        }

        // Valida nome unico, excluindo o proprio ID
        validateUniqueName(request.getName(), id);

        // Atualiza os campos
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        log.info("Categoria atualizada: {}", updated.getName());

        return CategoryResponse.fromEntity(updated);
    }

    /**
     * Desativa uma categoria (soft delete).
     * Nao apaga do banco, apenas marca como inativa.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Desativando categoria com id: {}", id);

        Category category = findActiveById(id);

        if (category.isDefault()) {
            throw new BusinessException("Categorias padrao nao podem ser excluidas");
        }

        // Soft delete: marca como inativa em vez de apagar
        category.setActive(false);
        categoryRepository.save(category);

        log.info("Categoria desativada: {}", category.getName());
    }

    // ========== METODOS AUXILIARES (PRIVATE) ==========

    /**
     * Busca uma categoria ativa pelo ID ou lanca excecao.
     * Metodo reutilizado por findById, update e delete.
     */
    private Category findActiveById(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    /**
     * Valida se o nome da categoria e unico.
     * O parametro excludeId e usado no update para nao comparar consigo mesma.
     */
    private void validateUniqueName(String name, Long excludeId) {
        boolean exists;

        if (excludeId == null) {
            exists = categoryRepository.existsByNameIgnoreCase(name.trim());
        } else {
            exists = categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
        }

        if (exists) {
            throw new BusinessException("Ja existe uma categoria com o nome: " + name);
        }
    }
}
