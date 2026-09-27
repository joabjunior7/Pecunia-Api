package com.fintrack.enums;

import lombok.Getter;

/**
 * Tipos de conta suportados pelo FinTrack.
 *
 * WALLET      -> Dinheiro físico, carteira
 * BANK        -> Conta corrente ou poupança
 * CREDIT_CARD -> Cartão de crédito
 */
@Getter
public enum AccountType {

    WALLET("Carteira"),
    BANK("Conta Bancária"),
    CREDIT_CARD("Cartão de Crédito");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }
}
