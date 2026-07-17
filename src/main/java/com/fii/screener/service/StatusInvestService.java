package com.fii.screener.service;

import com.fii.screener.model.DimFii;
import com.fii.screener.model.FactScreener;
import com.fii.screener.repository.DimFiiRepository;
import com.fii.screener.repository.FactScreenerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.net.URI;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusInvestService {

    private final DimFiiRepository dimFiiRepository;
    private final FactScreenerRepository factScreenerRepository;
    private final RestTemplate restTemplate;

    @Value("${fii.tickers}")
    private String tickersConfig;

    public void updateFiiData() {
        if (tickersConfig == null || tickersConfig.isEmpty()) return;

        Set<String> targetTickers = Arrays.stream(tickersConfig.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        log.info("Iniciando atualização de FIIs para: {}", targetTickers);

        // Limpeza de tickers que não deveriam estar no banco
        dimFiiRepository.findAll().forEach(f -> {
            if (!targetTickers.contains(f.getTicker())) {
                // Remove histórico primeiro, se desejar (ou não remove)
                List<FactScreener> facts = factScreenerRepository.findByFiiTicker(f.getTicker());
                factScreenerRepository.deleteAll(facts);
                dimFiiRepository.delete(f);
                log.info("Removido ticker obsoleto: {}", f.getTicker());
            }
        });

        try {
            String csvUrl = "https://statusinvest.com.br/category/advancedsearchresultexport"
                    + "?search=%7B%22CategoryType%22%3A2%7D&CategoryType=2";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Buscando dados via StatusInvest CSV export...");
            URI uri = URI.create(csvUrl);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String csvBody = response.getBody();

            if (csvBody == null || csvBody.isEmpty()) {
                log.error("Resposta vazia do StatusInvest");
                return;
            }

            Map<String, Map<String, String>> parsedData = parseCsv(csvBody);
            log.info("Total de FIIs recebidos do StatusInvest: {}", parsedData.size());

            int successCount = 0;
            for (String ticker : targetTickers) {
                Map<String, String> row = parsedData.get(ticker);
                if (row != null) {
                    saveFiiFromCsv(row, ticker);
                    successCount++;
                } else {
                    log.warn("Ticker {} não encontrado nos dados do StatusInvest", ticker);
                }
            }

            log.info("Atualização finalizada. Atualizados: {}/{}", successCount, targetTickers.size());

        } catch (Exception e) {
            log.error("Erro ao buscar dados do StatusInvest: {}", e.getMessage());
            throw new RuntimeException("Falha na atualização: " + e.getMessage(), e);
        }
    }

    private Map<String, Map<String, String>> parseCsv(String csv) {
        Map<String, Map<String, String>> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return result;

            String[] headers = headerLine.split(";");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim().toUpperCase();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(";", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    row.put(headers[i], values[i].trim());
                }

                String ticker = row.get("TICKER");
                if (ticker != null && !ticker.isEmpty()) {
                    result.put(ticker.trim().toUpperCase(), row);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao fazer parse do CSV: {}", e.getMessage());
        }
        return result;
    }

    private void saveFiiFromCsv(Map<String, String> data, String symbol) {
        Double price = parseDouble(data.get("PRECO"));
        Double lastDividend = parseDouble(data.get("ULTIMO DIVIDENDO"));
        Double dy = parseDouble(data.get("DY"));
        // StatusInvest export não tem a coluna Vacância de forma clara no CSV base, 
        // mas vamos colocar 0.0 caso não encontre
        Double vacancy = parseDouble(data.get("VACANCIA FISICA")); 
        Double liquidez = parseDouble(data.get("LIQUIDEZ MEDIA DIARIA"));
        Double pvpValue = parseDouble(data.get("P/VP"));
        String segment = data.get("SEGMENTO"); // Se existir, caso contrário fica null
        String type = data.get("GESTAO");

        // Atualiza ou insere na dimensão
        DimFii dimFii = dimFiiRepository.findById(symbol).orElse(null);
        if (dimFii == null) {
            dimFii = DimFii.builder()
                    .ticker(symbol)
                    .nomeFundo(symbol) // Fallback for name since CSV doesn't have it
                    .segment(segment != null ? segment : "")
                    .tipoFundo(type != null ? type : "")
                    .build();
            dimFii = dimFiiRepository.save(dimFii);
        } else {
            // Atualiza propriedades caso mudem
            if (segment != null && !segment.isEmpty()) {
                dimFii.setSegment(segment);
            }
            if (type != null && !type.isEmpty()) {
                dimFii.setTipoFundo(type);
            }
            dimFii = dimFiiRepository.save(dimFii);
        }

        // Insere o fato (histórico do dia)
        FactScreener fact = FactScreener.builder()
                .fii(dimFii)
                .data(LocalDate.now())
                .precoAtual(price)
                .dividendYield(dy)
                .ultimoDividendo(lastDividend)
                .vacancia(vacancy)
                .liquidezDiaria(liquidez)
                .pvp(pvpValue)
                .build();

        factScreenerRepository.save(fact);
        log.info("Novo Fato Inserido para {}: Preço {}, DY {}, Vacância {}", symbol, price, dy, vacancy);
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return 0.0;
        try {
            String normalized = value
                    .replace(".", "")
                    .replace(",", ".");
            return Double.valueOf(normalized);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
