package com.arbitrage.controller;

import com.arbitrage.model.ArbitrageOpportunity;
import com.arbitrage.service.ArbitrageCalculator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arbitrage")
@CrossOrigin(origins = "*")
public class ArbitrageController {
    
    private final ArbitrageCalculator arbitrageCalculator;

    public ArbitrageController(ArbitrageCalculator arbitrageCalculator) {
        this.arbitrageCalculator = arbitrageCalculator;
    }

    @GetMapping("/opportunities")
    public ResponseEntity<List<ArbitrageOpportunity>> getOpportunities() {
        return ResponseEntity.ok(arbitrageCalculator.getOpportunities());
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearOpportunities() {
        arbitrageCalculator.clearOpportunities();
        return ResponseEntity.ok().build();
    }
}