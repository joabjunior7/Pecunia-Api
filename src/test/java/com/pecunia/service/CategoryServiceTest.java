package com.pecunia.service;

import com.pecunia.dto.request.CategoryRequest;
import com.pecunia.dto.response.CategoryResponse;
import com.pecunia.entity.Category;
import com.pecunia.exception.BusinessException;
import com.pecunia.exception.ResourceNotFoundException;
import com.pecunia.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CategoryService.
 *
 * --- CONCEITOS IMPORTANTES ---
 * @ExtendWith(MockitoExtension.class) -> Habilita o framework Mockito no JUnit 5.
 * @Mock -> Cria um dublê (mock) do repositório, sem conectar a banco de dados real.
 * @InjectMocks -> Instancia o CategoryService injetando os mocks criados dentro dele.
 * Padrão AAA: Arrange (Preparar) -> Act (Executar) -> Assert (Verificar).
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Category defaultCategory;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Alimentação")
                .description("Gastos com mercado e delivery")
                .isDefault(false)
                .active(true)
                .build();

        defaultCategory = Category.builder()
                .id(2L)
                .name("Salário")
                .description("Categoria padrão do sistema")
                .isDefault(true)
                .active(true)
                .build();

        request = CategoryRequest.builder()
                .name("Alimentação")
                .description("Gastos com mercado e delivery")
                .build();
    }

    @Test
    @DisplayName("Deve listar todas as categorias ativas com sucesso")
    void findAll_DeveRetornarCategoriasAtivas() {
        // Arrange
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(category));

        // Act
        List<CategoryResponse> result = categoryService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alimentação", result.get(0).getName());
        verify(categoryRepository, times(1)).findByActiveTrue();
    }

    @Test
    @DisplayName("Deve retornar categoria quando ID existir")
    void findById_QuandoExiste_DeveRetornarCategoria() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));

        // Act
        CategoryResponse result = categoryService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alimentação", result.getName());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando ID não existir")
    void findById_QuandoNaoExiste_DeveLancarExcecao() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    @DisplayName("Deve criar categoria quando os dados forem válidos e nome único")
    void create_QuandoValido_DeveSalvarECriar() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase("Alimentação")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        CategoryResponse result = categoryService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("Alimentação", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar criar categoria com nome já existente")
    void create_QuandoNomeDuplicado_DeveLancarBusinessException() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase("Alimentação")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.create(request));
        assertTrue(exception.getMessage().contains("Ja existe uma categoria"));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria quando dados forem válidos")
    void update_QuandoValido_DeveAtualizar() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Alimentação", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        CategoryResponse result = categoryService.update(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("Alimentação", result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar editar categoria padrão do sistema")
    void update_QuandoCategoriaPadrao_DeveLancarBusinessException() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.update(2L, request));
        assertTrue(exception.getMessage().contains("padrao nao podem ser editadas"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar categoria (soft delete) com sucesso")
    void delete_QuandoValido_DeveDesativar() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));

        // Act
        categoryService.delete(1L);

        // Assert
        assertFalse(category.isActive());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar excluir categoria padrão do sistema")
    void delete_QuandoCategoriaPadrao_DeveLancarBusinessException() {
        // Arrange
        when(categoryRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.delete(2L));
        assertTrue(exception.getMessage().contains("padrao nao podem ser excluidas"));
        verify(categoryRepository, never()).save(any());
    }
}
