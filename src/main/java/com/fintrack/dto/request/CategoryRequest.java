package com.fintrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para criar/atualizar uma categoria.
 *
 * --- POR QUE USAR DTOs? ---
 *
 * 1. SEGURANCA: O cliente nao pode enviar campos que nao deveria
 *    (ex: forcar isDefault = true ou manipular o createdAt)
 *
 * 2. DESACOPLAMENTO: Se a entidade mudar (ex: novo campo), a API
 *    nao quebra porque o contrato e o DTO, nao a entidade
 *
 * 3. VALIDACAO: As anotacoes de validacao ficam aqui, nao na entidade
 *
 * --- ANOTACOES DE VALIDACAO ---
 *
 * @NotBlank -> Nao pode ser null, vazio "" ou so espacos "   "
 * @Size     -> Limita o tamanho do texto
 *
 * Quando o controller recebe esse DTO com @Valid, o Spring valida
 * automaticamente e, se tiver erro, o GlobalExceptionHandler retorna
 * uma resposta formatada.
 *
 * Pergunta de entrevista: "Qual a diferenca entre @NotNull, @NotEmpty e @NotBlank?"
 * Resposta:
 * - @NotNull  -> so verifica se nao e null (aceita "" e "   ")
 * - @NotEmpty -> nao pode ser null nem vazio "" (aceita "   ")
 * - @NotBlank -> nao pode ser null, vazio "" nem so espacos "   "
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "O nome da categoria e obrigatorio")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    private String name;

    @Size(max = 200, message = "A descricao deve ter no maximo 200 caracteres")
    private String description;
}
