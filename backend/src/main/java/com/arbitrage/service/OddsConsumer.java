package com.arbitrage.service;

import com.arbitrage.model.BettingOdds;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OddsConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(OddsConsumer.class);
    private final ArbitrageCalculator arbitrageCalculator;
    private final ObjectMapper objectMapper;

    public OddsConsumer(ArbitrageCalculator arbitrageCalculator, ObjectMapper objectMapper) {
        this.arbitrageCalculator = arbitrageCalculator;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "betting-odds", groupId = "arbitrage-bot")
    public void consumeOdds(String message) {
        try {
            BettingOdds odds = objectMapper.readValue(message, BettingOdds.class);
            log.info("Received odds: {} - {} @ {}", 
                odds.getEvent(), odds.getOdds(), odds.getSportsbook());
            
            arbitrageCalculator.processOdds(odds);
        } catch (Exception e) {
            log.error("Error processing odds: {}", e.getMessage());
        }
    }
}