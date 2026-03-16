package com.example.meta_backend.service.llm;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    @Value("${llm.enabled:false}")
    private boolean enabled;

    @Value("${llm.mode:ollama}")
    private String mode;

    @Value("${llm.url:http://localhost:11434}")
    private String url;

    @Value("${llm.model:qwen2.5-coder:7b}")
    private String model;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.timeout-seconds:120}")
    private int timeoutSeconds;

    private final WebClient webClient;

    public LlmClient(WebClient.Builder webClientBuilder) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder
                .exchangeStrategies(strategies)
                .build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String generate(String systemPrompt, String userPrompt) {
        log.info("=== LLM CALL === model={}, mode={}, url={}", model, mode, url);
        try {
            String result = "openai".equalsIgnoreCase(mode)
                    ? callOpenAiCompatible(systemPrompt, userPrompt)
                    : callOllama(systemPrompt, userPrompt);
            log.info("=== LLM OK === resposta tem {} chars", result == null ? 0 : result.length());
            return result;
        } catch (Exception e) {
            log.error("=== LLM ERRO === {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String callOllama(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", systemPrompt + "\n\n" + userPrompt,
                "stream", false
        );

        Map<String, Object> response = webClient.post()
                .uri(url + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        if (response == null) throw new RuntimeException("Resposta nula do Ollama");
        return (String) response.get("response");
    }

    @SuppressWarnings("unchecked")
    private String callOpenAiCompatible(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        Map<String, Object> response = webClient.post()
                .uri(url + "/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        if (response == null) throw new RuntimeException("Resposta nula da API");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
