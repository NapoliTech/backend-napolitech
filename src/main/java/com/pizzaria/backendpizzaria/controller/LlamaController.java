package com.pizzaria.backendpizzaria.controller;

import com.pizzaria.backendpizzaria.service.Llama.LlamaService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/llama")
public class LlamaController {

    private final LlamaService llamaService;

    public LlamaController(LlamaService llamaService) {
        this.llamaService = llamaService;
    }

    @PostMapping
    public ResponseEntity<?> enviarPrompt(@RequestBody Map<String, Object> payload) {
        if (!llamaService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("erro", "LLAMA_API_URL não configurada"));
        }
        try {
            ResponseEntity<Object> response = llamaService.enviarPrompt(payload);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("erro", "Falha ao comunicar com o agente Llama", "detalhe", e.getMessage()));
        }
    }
}
