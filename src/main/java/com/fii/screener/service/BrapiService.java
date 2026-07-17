package com.fii.screener.service;

import com.fii.screener.dto.BrapiFiiDTO;
import com.fii.screener.dto.BrapiResponseDTO;
import com.fii.screener.model.DimFii;
import com.fii.screener.model.FactScreener;
import com.fii.screener.repository.DimFiiRepository;
import com.fii.screener.repository.FactScreenerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fii.screener.client.BrapiApiClient;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrapiService {

    private final DimFiiRepository dimFiiRepository;
    private final FactScreenerRepository factScreenerRepository;
    private final BrapiApiClient brapiApiClient;

    @Value("${fii.tickers}")
    private String tickers;

    public void updateFiiData() {
        try {
            BrapiResponseDTO response = brapiApiClient.fetchFiiIndicators(tickers);
            if (response != null && response.fiis() != null) {
                List<BrapiFiiDTO> fiisData = response.fiis();

                for (BrapiFiiDTO data : fiisData) {
                    String symbol = data.symbol().toUpperCase();
                    
                    if (data.price() == null) {
                        log.warn("Skipping FII {}: Price is missing", symbol);
                        continue;
                    }

                    Double price = data.price();
                    Double dy = data.dividendYield12m() != null ? data.dividendYield12m() : 0.0;
                    // BrapiDTO might not have last dividend directly. Assuming 0.0 or calculated if needed.
                    Double lastDividend = 0.0; 
                    Double vacancy = data.vacancy() != null ? data.vacancy() : 0.0;
                    
                    String segment = data.segment();
                    String type = data.type();
                    String nome = data.name() != null ? data.name() : symbol;

                    // Atualiza ou cria a dimensão
                    DimFii dimFii = dimFiiRepository.findById(symbol).orElse(null);
                    if (dimFii == null) {
                        dimFii = DimFii.builder()
                                .ticker(symbol)
                                .nomeFundo(nome)
                                .segment(segment != null ? segment : "")
                                .tipoFundo(type != null ? type : "")
                                .build();
                        dimFii = dimFiiRepository.save(dimFii);
                    } else {
                        if (segment != null && !segment.isEmpty()) {
                            dimFii.setSegment(segment);
                        }
                        if (type != null && !type.isEmpty()) dimFii.setTipoFundo(type);
                        dimFii = dimFiiRepository.save(dimFii);
                    }

                    // Cria o fato diário
                    FactScreener fact = FactScreener.builder()
                            .fii(dimFii)
                            .data(LocalDate.now())
                            .precoAtual(price)
                            .dividendYield(dy)
                            .ultimoDividendo(lastDividend)
                            .vacancia(vacancy)
                            .liquidezDiaria(0.0) // Brapi doesn't provide this by default in DTO, fallback to 0.0
                            .pvp(data.priceToNav() != null ? data.priceToNav() : 0.0)
                            .build();

                    factScreenerRepository.save(fact);
                    log.info("Updated Fact for FII via Brapi: {}", symbol);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching data from Brapi: {}", e.getMessage());
        }
    }
}
