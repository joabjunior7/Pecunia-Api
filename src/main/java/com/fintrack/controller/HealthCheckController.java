package com.fintrack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller de Health Check.
 *
 * Serve para verificar se a API esta rodando corretamente.
 * Em entrevistas, isso mostra que voce se preocupa com monitoramento
 * e observabilidade da aplicacao.
 */
@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", "FinTrack API",
            "version", "0.0.1-SNAPSHOT",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
