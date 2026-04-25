package com.pizzaria.backendpizzaria.service.Llama;

import com.pizzaria.backendpizzaria.config.LlamaProperties;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class LlamaService {

    private final RestTemplate restTemplate;
    private final LlamaProperties llamaProperties;

    public LlamaService(RestTemplateBuilder restTemplateBuilder, LlamaProperties llamaProperties) {
        this.llamaProperties = llamaProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(llamaProperties.getApiUrl());
    }

    public ResponseEntity<Object> enviarPrompt(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(llamaProperties.getApiKey())) {
            headers.setBearerAuth(llamaProperties.getApiKey());
        }
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        return restTemplate.postForEntity(llamaProperties.getApiUrl(), request, Object.class);
    }
}
