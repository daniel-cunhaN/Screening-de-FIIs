package com.fii.screener.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fii.screener.dto.BrapiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Slf4j
public class BrapiApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${brapi.token}")
    private String token;

    public BrapiApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Instanciar o HttpClient do Java 11+ aqui.
        // O HttpClient é thread-safe e deve ser reutilizado para melhor performance.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public BrapiResponseDTO fetchFiiIndicators(String tickers) {
        if (token == null || token.isEmpty() || "${BRAPI_TOKEN}".equals(token)) {
            log.error("Brapi token is not configured.");
            throw new IllegalStateException("Brapi token is not configured.");
        }

        String url = String.format("https://brapi.dev/api/v2/fii/indicators?symbols=%s&token=%s", tickers, token);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10)) // Timeout para não travar a thread indefinidamente
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // Parseia o JSON retornado para o DTO específico
                return objectMapper.readValue(response.body(), BrapiResponseDTO.class);
            } else {
                log.error("Error fetching data from Brapi. Status Code: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Brapi API request failed with status: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted", e);
            throw new RuntimeException("Request interrupted", e);
        } catch (Exception e) {
            log.error("Exception occurred while calling Brapi API", e);
            throw new RuntimeException("Error fetching FII data from Brapi API", e);
        }
    }
}
