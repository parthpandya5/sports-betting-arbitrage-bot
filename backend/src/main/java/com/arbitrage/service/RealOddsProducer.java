package com.arbitrage.service;

import com.arbitrage.model.BettingOdds;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RealOddsProducer {
    
    private static final Logger log = LoggerFactory.getLogger(RealOddsProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Value("${odds.api.key:}")
    private String apiKey;
    
    @Value("${odds.api.enabled:false}")
    private boolean apiEnabled;
    
    private static final String ODDS_API_BASE = "https://api.the-odds-api.com/v4";
    private int requestCount = 0;
    
    public RealOddsProducer(KafkaTemplate<String, String> kafkaTemplate, 
                           ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }
    
    // Fetch odds every 8 minutes (480,000ms) - ~180 requests/day, well under 500/month limit
    @Scheduled(fixedRate = 480000, initialDelay = 5000)
    public void fetchRealOdds() {
        if (!apiEnabled) {
            log.warn("⚠️  Real odds API is DISABLED. Enable in application.properties");
            log.warn("⚠️  Set odds.api.key=YOUR_KEY and odds.api.enabled=true");
            return;
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("❌ API key not configured! Set odds.api.key in application.properties");
            return;
        }
        
        try {
            log.info("🔄 Fetching real odds from The Odds API (Request #{})", ++requestCount);
            
            // Fetch NBA odds
            fetchSportOdds("basketball_nba", "NBA");
            
            // Fetch NFL odds  
            fetchSportOdds("americanfootball_nfl", "NFL");
            
            // Optionally add more sports:
            // fetchSportOdds("icehockey_nhl", "NHL");
            // fetchSportOdds("baseball_mlb", "MLB");
            
            log.info("✅ Completed odds fetch #{}", requestCount);
            
        } catch (Exception e) {
            log.error("❌ Error fetching real odds: {}", e.getMessage(), e);
        }
    }
    
    private void fetchSportOdds(String sportKey, String sportName) {
        try {
            String url = String.format(
                "%s/sports/%s/odds/?apiKey=%s&regions=us&markets=h2h&oddsFormat=decimal&bookmakers=draftkings,fanduel,betmgm,caesars,pointsbet,betrivers",
                ODDS_API_BASE, sportKey, apiKey
            );
            
            log.info("📡 Fetching {} odds...", sportName);
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                log.warn("⚠️  Empty response for {}", sportName);
                return;
            }
            
            JsonNode events = objectMapper.readTree(response);
            
            if (events == null || events.size() == 0) {
                log.warn("⚠️  No {} events found (might be off-season)", sportName);
                return;
            }
            
            log.info("📥 Fetched {} {} events", events.size(), sportName);
            int oddsPublished = 0;
            
            for (JsonNode event : events) {
                String homeTeam = event.get("home_team").asText();
                String awayTeam = event.get("away_team").asText();
                String eventName = awayTeam + " @ " + homeTeam;
                
                JsonNode bookmakers = event.get("bookmakers");
                
                if (bookmakers == null || bookmakers.size() == 0) {
                    continue;
                }
                
                for (JsonNode bookmaker : bookmakers) {
                    String sportsbookName = bookmaker.get("title").asText();
                    JsonNode markets = bookmaker.get("markets");
                    
                    for (JsonNode market : markets) {
                        if (!market.get("key").asText().equals("h2h")) continue;
                        
                        JsonNode outcomes = market.get("outcomes");
                        
                        for (JsonNode outcome : outcomes) {
                            String teamName = outcome.get("name").asText();
                            double odds = outcome.get("price").asDouble();
                            
                            BettingOdds bettingOdds = new BettingOdds(
                                sportName,
                                eventName,
                                "Moneyline",
                                sportsbookName,
                                odds,
                                teamName,
                                System.currentTimeMillis()
                            );
                            
                            String message = objectMapper.writeValueAsString(bettingOdds);
                            kafkaTemplate.send("betting-odds", message);
                            oddsPublished++;
                            
                            log.debug("📤 {} - {} @ {} ({:.2f})", 
                                eventName, teamName, sportsbookName, odds);
                        }
                    }
                }
            }
            
            log.info("✅ Published {} odds entries for {}", oddsPublished, sportName);
            
        } catch (Exception e) {
            log.error("❌ Error fetching {} odds: {}", sportName, e.getMessage());
        }
    }
}