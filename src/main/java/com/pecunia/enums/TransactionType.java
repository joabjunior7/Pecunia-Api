package com.pecunia.enums;

import lombok.Getter;

/**
 * Tipo da movimentação financeira no Pecunia.
 *
 * EXPENSE -> Despesa (subtrai do saldo da conta)
 * INCOME  -> Receita (soma ao saldo da conta)
 */
@Getter
public enum TransactionType {

    EXPENSE("Despesa"),
    INCOME("Receita");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
}
