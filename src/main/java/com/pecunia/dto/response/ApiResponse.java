package com.pecunia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Classe padrao de resposta da API.
 *
 * Todas as respostas seguem o mesmo formato:
 * - success: se a operacao foi bem-sucedida
 * - message: mensagem descritiva
 * - data: os dados retornados (generico)
 * - timestamp: quando a resposta foi gerada
 *
 * O @JsonInclude(NON_NULL) faz com que campos nulos nao aparecam no JSON.
 * Isso deixa a resposta mais limpa.
 *
 * @param <T> o tipo dos dados retornados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Cria uma resposta de sucesso com dados.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Cria uma resposta de sucesso com mensagem e dados.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Cria uma resposta de erro.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
