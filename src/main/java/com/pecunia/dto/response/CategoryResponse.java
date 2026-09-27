package com.pecunia.dto.response;

import com.pecunia.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de saida de Category.
 *
 * Controla EXATAMENTE o que o cliente recebe.
 * Perceba que nao expoe o campo 'active' (nao faz sentido pro cliente,
 * ja que so retornamos categorias ativas) nem o 'updatedAt'.
 *
 * O metodo estatico fromEntity() converte Entity -> DTO.
 * Esse padrao e muito comum e evita que o controller conheca
 * detalhes da entidade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private boolean isDefault;
    private LocalDateTime createdAt;

    /**
     * Converte uma entidade Category para o DTO de resposta.
     *
     * Essa abordagem e mais simples que usar ModelMapper ou MapStruct.
     * Para projetos maiores, ModelMapper ou MapStruct sao melhores,
     * mas para o tamanho do Pecunia, metodos estaticos resolvem bem.
     */
    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isDefault(category.isDefault())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
