package com.fii.screener.service;

import com.fii.screener.model.Fii;
import com.fii.screener.repository.FiiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrapiService {

    private final FiiRepository fiiRepository;
    private final RestTemplate restTemplate;

    @Value("${brapi.token}")
    private String token;

    @Value("${fii.tickers}")
    private String tickers;

    public void updateFiiData() {
        if (isTokenMissing()) {
            log.error("Brapi token is not configured. Please set the BRAPI_TOKEN environment variable.");
            return;
        }

        String url = String.format("https://brapi.dev/api/v2/fii/indicators?symbols=%s&token=%s", tickers, token);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("fiis")) {
                processFiisData((List<Map<String, Object>>) response.get("fiis"));
            }
        } catch (Exception e) {
            log.error("Error fetching data from Brapi: {}", e.getMessage());
        }
    }

    private boolean isTokenMissing() {
        return token == null || token.isEmpty() || "${BRAPI_TOKEN}".equals(token);
    }

    private void processFiisData(List<Map<String, Object>> fiisData) {
        for (Map<String, Object> data : fiisData) {
            String symbol = (String) data.get("symbol");
            
            if (data.get("price") == null) {
                log.warn("Skipping FII {}: Price is missing", symbol);
                continue;
            }

            Fii fii = Fii.builder()
                    .ticker(symbol)
                    .price(toDouble(data.get("price")))
                    .dividendYield(toDouble(data.get("dividendYield12m")))
                    .dividendYield1m(toDouble(data.get("dividendYield1m")))
                    .pvp(toDouble(data.get("priceToNav")))
                    .vacancy(toDouble(data.get("vacancy")))
                    .netWorth(toDouble(data.get("netWorth")))
                    .equityValue(toDouble(data.get("equityValue")))
                    .segment((String) data.get("segment"))
                    .type((String) data.get("type"))
                    .lastUpdate(LocalDateTime.now())
                    .build();

            fiiRepository.save(fii);
            log.info("Updated FII: {}", symbol);
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Could not convert value to Double: {}", value);
            return 0.0;
        }
    }
}
