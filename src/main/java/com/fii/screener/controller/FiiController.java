package com.fii.screener.controller;

import com.fii.screener.model.Fii;
import com.fii.screener.repository.FiiRepository;
import com.fii.screener.service.StatusInvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fiis")
@RequiredArgsConstructor
public class FiiController {

    private final FiiRepository fiiRepository;
    private final StatusInvestService statusInvestService;

    @GetMapping
    public ResponseEntity<List<Fii>> getAllFiis() {
        return ResponseEntity.ok(fiiRepository.findAll());
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<Fii> getFiiByTicker(@PathVariable String ticker) {
        return fiiRepository.findById(ticker.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
}
