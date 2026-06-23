package com.fii.screener.service;

import com.fii.screener.dto.BrapiFiiDTO;
import com.fii.screener.dto.BrapiResponseDTO;
import com.fii.screener.model.Fii;
import com.fii.screener.repository.FiiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fii.screener.client.BrapiApiClient;

import java.time.LocalDateTime;
import java.util.List;

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
            BrapiResponseDTO response = brapiApiClient.fetchFiiIndicators(tickers);
            if (response != null && response.fiis() != null) {
                List<BrapiFiiDTO> fiisData = response.fiis();

                for (BrapiFiiDTO data : fiisData) {
                    String symbol = data.symbol();
                    
                    if (data.price() == null) {
                        log.warn("Skipping FII {}: Price is missing", symbol);
                        continue;
                    }

                    Double price = data.price() != null ? data.price() : 0.0;
                    Double dy = data.dividendYield12m() != null ? data.dividendYield12m() : 0.0;
                    Double pvp = data.priceToNav() != null ? data.priceToNav() : 0.0;
                    Double vacancy = data.vacancy() != null ? data.vacancy() : 0.0;
                    Double netWorth = data.netWorth() != null ? data.netWorth() : 0.0;
                    Double equityValue = data.equityValue() != null ? data.equityValue() : 0.0;
                    String segment = data.segment();
                    String type = data.type();

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
