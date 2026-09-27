package com.pecunia.dto.request;

import com.pecunia.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para criação e atualização de conta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "O nome da conta é obrigatório")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    private String name;

    @NotNull(message = "O tipo da conta é obrigatório (WALLET, BANK ou CREDIT_CARD)")
    private AccountType type;

    /**
     * Saldo inicial da conta. Se não informado, inicia como 0.00.
     */
    private BigDecimal initialBalance;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    private String description;
}
