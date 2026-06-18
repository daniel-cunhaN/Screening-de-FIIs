package com.fii.screener.service;

import com.fii.screener.model.Fii;
import com.fii.screener.repository.FiiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fii.screener.client.BrapiApiClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrapiService {

    private final FiiRepository fiiRepository;
    private final BrapiApiClient brapiApiClient;

    @Value("${fii.tickers}")
    private String tickers;

    public void updateFiiData() {
        try {
            Map<String, Object> response = brapiApiClient.fetchFiiIndicators(tickers);
            if (response != null && response.containsKey("fiis")) {
                List<Map<String, Object>> fiisData = (List<Map<String, Object>>) response.get("fiis");

                for (Map<String, Object> data : fiisData) {
                    String symbol = (String) data.get("symbol");
                    
                    if (data.get("price") == null) {
                        log.warn("Skipping FII {}: Price is missing", symbol);
                        continue;
                    }

                    Double price = data.get("price") != null ? Double.valueOf(data.get("price").toString()) : 0.0;
                    Double dy = data.get("dividendYield12m") != null ? Double.valueOf(data.get("dividendYield12m").toString()) : 0.0;
                    Double pvp = data.get("priceToNav") != null ? Double.valueOf(data.get("priceToNav").toString()) : 0.0;
                    Double vacancy = data.get("vacancy") != null ? Double.valueOf(data.get("vacancy").toString()) : 0.0;
                    Double netWorth = data.get("netWorth") != null ? Double.valueOf(data.get("netWorth").toString()) : 0.0;
                    Double equityValue = data.get("equityValue") != null ? Double.valueOf(data.get("equityValue").toString()) : 0.0;
                    String segment = (String) data.get("segment");
                    String type = (String) data.get("type");

                    Fii fii = Fii.builder()
                            .ticker(symbol)
                            .price(price)
                            .dividendYield(dy)
                            .pvp(pvp)
                            .vacancy(vacancy)
                            .netWorth(netWorth)
                            .equityValue(equityValue)
                            .segment(segment)
                            .type(type)
                            .lastUpdate(LocalDateTime.now())
                            .build();

                    fiiRepository.save(fii);
                    log.info("Updated FII: {}", symbol);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching data from Brapi: {}", e.getMessage());
        }
    }
}
