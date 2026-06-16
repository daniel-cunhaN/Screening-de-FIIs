package com.fii.screener.service;

import com.fii.screener.model.Fii;
import com.fii.screener.repository.FiiRepository;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusInvestService {

    private final FiiRepository fiiRepository;
    private final RestTemplate restTemplate;

    @Value("${fii.tickers}")
    private String tickersConfig;

    /**
     * Busca dados de todos os FIIs via endpoint CSV do StatusInvest
     * (que retorna TODOS os FIIs de uma vez) e salva apenas os tickers configurados.
     */
    public void updateFiiData() {
        if (tickersConfig == null || tickersConfig.isEmpty()) return;

        Set<String> targetTickers = Arrays.stream(tickersConfig.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        log.info("Iniciando atualização de FIIs para: {}", targetTickers);

        // Limpeza de tickers que não deveriam estar no banco
        fiiRepository.findAll().forEach(f -> {
            if (!targetTickers.contains(f.getTicker())) {
                fiiRepository.delete(f);
                log.info("Removido ticker obsoleto: {}", f.getTicker());
            }
        });

        try {
            // Busca todos os FIIs via endpoint CSV do StatusInvest
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

            // Parse do CSV
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

    /**
     * Parse do CSV retornado pelo StatusInvest.
     * Formato: TICKER;PRECO;ULTIMO DIVIDENDO;DY;VALOR PATRIMONIAL COTA;P/VP;...
     */
    private Map<String, Map<String, String>> parseCsv(String csv) {
        Map<String, Map<String, String>> result = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return result;

            String[] headers = headerLine.split(";");
            // Normaliza nomes das colunas
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
        Double dy1m = (price > 0.0) ? (lastDividend / price) * 100.0 : 0.0;

        Fii fii = Fii.builder()
                .ticker(symbol)
                .price(price)
                .dividendYield(parseDouble(data.get("DY")))
                .dividendYield1m(dy1m)
                .pvp(parseDouble(data.get("P/VP")))
                .vacancy(0.0)
                .netWorth(parseDouble(data.get("PATRIMONIO")))
                .equityValue(parseDouble(data.get("VALOR PATRIMONIAL COTA")))
                .segment(null) // CSV não inclui segmento
                .type(data.get("GESTAO"))
                .lastUpdate(LocalDateTime.now())
                .build();

        fiiRepository.save(fii);
        log.info("FII Salvo: {} | Preço: {} | DY: {}% | P/VP: {}", symbol, price,
                parseDouble(data.get("DY")), parseDouble(data.get("P/VP")));
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return 0.0;
        try {
            // StatusInvest usa formato brasileiro: 1.234,56
            String normalized = value
                    .replace(".", "")    // remove separador de milhar
                    .replace(",", ".");  // troca vírgula decimal por ponto
            return Double.valueOf(normalized);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
