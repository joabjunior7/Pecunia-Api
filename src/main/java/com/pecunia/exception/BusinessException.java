package com.pecunia.exception;

/**
 * Excecao lancada quando uma regra de negocio e violada.
 * Exemplo: tentar excluir uma categoria que tem transacoes vinculadas.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
