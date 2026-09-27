package com.fintrack.dto.response;

import com.fintrack.entity.Account;
import com.fintrack.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para retorno das informações de uma conta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String name;
    private AccountType type;
    private String typeDescription;
    private BigDecimal balance;
    private String description;
    private LocalDateTime createdAt;

    public static AccountResponse fromEntity(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .typeDescription(account.getType() != null ? account.getType().getDescription() : null)
                .balance(account.getBalance())
                .description(account.getDescription())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
