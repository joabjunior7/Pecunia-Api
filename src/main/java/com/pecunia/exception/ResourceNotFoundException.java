package com.pecunia.exception;

/**
 * Excecao lancada quando um recurso nao e encontrado no banco de dados.
 * Exemplo: buscar uma categoria por ID que nao existe.
 *
 * Estende RuntimeException (unchecked exception) porque nao queremos
 * ser obrigados a fazer try/catch em todo lugar.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " nao encontrado(a) com id: " + id);
    }
}
