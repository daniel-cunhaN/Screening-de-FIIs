package com.fii.screener.controller;

import com.fii.screener.dto.FiiResponseDTO;
import com.fii.screener.model.DimFii;
import com.fii.screener.model.FactScreener;
import com.fii.screener.repository.DimFiiRepository;
import com.fii.screener.repository.FactScreenerRepository;
import com.fii.screener.service.StatusInvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fiis")
@RequiredArgsConstructor
public class FiiController {

    private final DimFiiRepository dimFiiRepository;
    private final FactScreenerRepository factScreenerRepository;
    private final StatusInvestService statusInvestService;

    @GetMapping
    public ResponseEntity<List<FiiResponseDTO>> getAllFiis() {
        List<DimFii> dimFiis = dimFiiRepository.findAll();
        List<FiiResponseDTO> response = new ArrayList<>();
        
        for (DimFii dim : dimFiis) {
            List<FactScreener> facts = factScreenerRepository.findByFiiTicker(dim.getTicker());
            FactScreener latest = facts.stream().max(Comparator.comparing(FactScreener::getData)).orElse(null);
            
            response.add(buildDto(dim, latest));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<FiiResponseDTO> getFiiByTicker(@PathVariable String ticker) {
        return dimFiiRepository.findById(ticker.toUpperCase())
                .map(dim -> {
                    List<FactScreener> facts = factScreenerRepository.findByFiiTicker(dim.getTicker());
                    FactScreener latest = facts.stream().max(Comparator.comparing(FactScreener::getData)).orElse(null);
                    return ResponseEntity.ok(buildDto(dim, latest));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{ticker}/historico")
    public ResponseEntity<List<FactScreener>> getFiiHistory(@PathVariable String ticker) {
        return ResponseEntity.ok(factScreenerRepository.findByFiiTicker(ticker.toUpperCase()));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> triggerUpdate() {
        try {
            statusInvestService.updateFiiData();
            return ResponseEntity.ok(Map.of("message", "FII data updated successfully from StatusInvest API."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update FII data: " + e.getMessage()));
        }
    }

    private FiiResponseDTO buildDto(DimFii dim, FactScreener latest) {
        return FiiResponseDTO.builder()
                .ticker(dim.getTicker())
                .nomeFundo(dim.getNomeFundo())
                .segment(dim.getSegment())
                .tipoFundo(dim.getTipoFundo())
                .price(latest != null ? latest.getPrecoAtual() : 0.0)
                .dividendYield(latest != null ? latest.getDividendYield() : 0.0)
                .ultimoDividendo(latest != null ? latest.getUltimoDividendo() : 0.0)
                .vacancy(latest != null ? latest.getVacancia() : 0.0)
                .lastUpdate(latest != null ? latest.getData() : null)
                .build();
    }
}
