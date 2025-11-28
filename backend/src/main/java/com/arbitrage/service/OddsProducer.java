package com.arbitrage.service;

import com.arbitrage.model.BettingOdds;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@Service
public class OddsProducer {
    
    private static final Logger log = LoggerFactory.getLogger(OddsProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private static final String[] SPORTS = {"NBA", "NFL", "NHL", "MLB", "Soccer"};
    private static final String[] NBA_GAMES = {
        "Lakers vs Warriors", "Celtics vs Heat", "Nets vs Bucks", 
        "Mavericks vs Suns", "Nuggets vs Clippers"
    };
    private static final String[] NFL_GAMES = {
        "Chiefs vs Bills", "Eagles vs Cowboys", "49ers vs Seahawks",
        "Ravens vs Bengals", "Packers vs Vikings"
    };
    private static final String[] SPORTSBOOKS = {
        "DraftKings", "FanDuel", "BetMGM", "Caesars", "PointsBet", "BetRivers"
    };
    private static final String[] MARKETS = {"Moneyline", "Spread", "Total"};

    public OddsProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Run every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void generateOdds() {
        try {
            String sport = SPORTS[random.nextInt(SPORTS.length)];
            String event = sport.equals("NBA") ? 
                NBA_GAMES[random.nextInt(NBA_GAMES.length)] : 
                NFL_GAMES[random.nextInt(NFL_GAMES.length)];
            String market = MARKETS[random.nextInt(MARKETS.length)];
            String sportsbook = SPORTSBOOKS[random.nextInt(SPORTSBOOKS.length)];
            
            // Generate odds between 1.5 and 3.5
            // Occasionally create arbitrage opportunities
            double baseOdds = 1.5 + (random.nextDouble() * 2.0);
            
            // 30% chance to create potential arbitrage
            if (random.nextDouble() < 0.3) {
                baseOdds = 2.0 + (random.nextDouble() * 0.5); // Tighter odds for arb
            }
            
            String outcome = event.split(" vs ")[random.nextInt(2)];
            
            BettingOdds odds = new BettingOdds(
                sport,
                event,
                market,
                sportsbook,
                Math.round(baseOdds * 100.0) / 100.0,
                outcome,
                System.currentTimeMillis()
            );
            
            String message = objectMapper.writeValueAsString(odds);
            kafkaTemplate.send("betting-odds", message);
            
            log.info("📤 Sent odds: {} - {} @ {} ({})", 
                event, odds.getOdds(), sportsbook, sport);
                
        } catch (Exception e) {
            log.error("Error generating odds: {}", e.getMessage());
        }
    }

    // Generate a burst of odds for the same event to increase arbitrage chances
    @Scheduled(fixedRate = 15000)
    public void generateArbitrageOpportunity() {
        try {
            String sport = "NBA";
            String event = NBA_GAMES[random.nextInt(NBA_GAMES.length)];
            String market = "Moneyline";
            String[] outcomes = event.split(" vs ");
            
            // Generate complementary odds that create arbitrage
            double odds1 = 2.05 + (random.nextDouble() * 0.2);
            double odds2 = 2.10 + (random.nextDouble() * 0.2);
            
            // Send odds from two different sportsbooks
            BettingOdds bet1 = new BettingOdds(
                sport, event, market,
                SPORTSBOOKS[0],
                Math.round(odds1 * 100.0) / 100.0,
                outcomes[0],
                System.currentTimeMillis()
            );
            
            BettingOdds bet2 = new BettingOdds(
                sport, event, market,
                SPORTSBOOKS[1],
                Math.round(odds2 * 100.0) / 100.0,
                outcomes[1],
                System.currentTimeMillis()
            );
            
            String msg1 = objectMapper.writeValueAsString(bet1);
            String msg2 = objectMapper.writeValueAsString(bet2);
            
            kafkaTemplate.send("betting-odds", msg1);
            Thread.sleep(100); // Small delay
            kafkaTemplate.send("betting-odds", msg2);
            
            log.info("🎯 Generated potential arbitrage for: {}", event);
            
        } catch (Exception e) {
            log.error("Error generating arbitrage: {}", e.getMessage());
        }
    }
}