package com.arbitrage.service;

import com.arbitrage.model.ArbitrageOpportunity;
import com.arbitrage.model.BettingOdds;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ArbitrageCalculator {
    
    private static final Logger log = LoggerFactory.getLogger(ArbitrageCalculator.class);
    private final Map<String, List<BettingOdds>> oddsCache = new ConcurrentHashMap<>();
    private final List<ArbitrageOpportunity> opportunities = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;

    public ArbitrageCalculator(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void processOdds(BettingOdds newOdds) {
        if (newOdds.getTimestamp() < System.currentTimeMillis() - 86400000) {
            log.info("Updating old timestamp to current time");
            newOdds.setTimestamp(System.currentTimeMillis());
        }
        
        String key = newOdds.getSport() + ":" + newOdds.getEvent() + ":" + newOdds.getMarket();
        
        oddsCache.computeIfAbsent(key, k -> new ArrayList<>()).add(newOdds);
        
        List<BettingOdds> eventOdds = oddsCache.get(key);
        log.info("Current odds count for {}: {}", key, eventOdds.size());
        
        if (eventOdds.size() >= 2) {
            findArbitrage(eventOdds);
        }
        
        cleanOldOdds();
    }

    private void findArbitrage(List<BettingOdds> oddsList) {
        log.info("Checking {} odds for arbitrage", oddsList.size());
        
        for (int i = 0; i < oddsList.size() - 1; i++) {
            for (int j = i + 1; j < oddsList.size(); j++) {
                BettingOdds odds1 = oddsList.get(i);
                BettingOdds odds2 = oddsList.get(j);
                
                log.info("Comparing {} @ {} ({}) vs {} @ {} ({})", 
                    odds1.getEvent(), odds1.getSportsbook(), odds1.getOdds(),
                    odds2.getEvent(), odds2.getSportsbook(), odds2.getOdds());
                
                if (odds1.getSportsbook().equals(odds2.getSportsbook())) {
                    log.info("Skipping - same sportsbook");
                    continue;
                }
                
                double impliedProb1 = 1.0 / odds1.getOdds();
                double impliedProb2 = 1.0 / odds2.getOdds();
                double totalProb = impliedProb1 + impliedProb2;
                
                log.info("Implied probs: {} + {} = {}", impliedProb1, impliedProb2, totalProb);
                
                if (totalProb < 1.0) {
                    double roi = ((1.0 / totalProb) - 1.0) * 100;
                    double stake1 = (impliedProb1 / totalProb) * 100;
                    double stake2 = (impliedProb2 / totalProb) * 100;
                    double profit = roi * 10;
                    
                    ArbitrageOpportunity opp = new ArbitrageOpportunity(
                        odds1.getSport(),
                        odds1.getEvent(),
                        odds1.getMarket(),
                        odds1.getSportsbook(),
                        odds1.getOdds(),
                        odds2.getSportsbook(),
                        odds2.getOdds(),
                        roi,
                        stake1,
                        stake2,
                        profit,
                        System.currentTimeMillis()
                    );
                    
                    opportunities.add(opp);
                    
                    // 🔥 REAL-TIME BROADCAST TO FRONTEND
                    messagingTemplate.convertAndSend("/topic/arbitrage", opp);
                    
                    log.info("✅ FOUND ARBITRAGE OPPORTUNITY: ROI {}%", String.format("%.2f", roi));
                } else {
                    log.info("No arbitrage - total prob {} >= 1.0", totalProb);
                }
            }
        }
    }

    private void cleanOldOdds() {
        long cutoff = System.currentTimeMillis() - 300000;
        oddsCache.values().forEach(list -> {
            int listBefore = list.size();
            list.removeIf(odds -> odds.getTimestamp() < cutoff);
            int listAfter = list.size();
            if (listBefore != listAfter) {
                log.info("Cleaned {} old odds entries", listBefore - listAfter);
            }
        });
    }

    public List<ArbitrageOpportunity> getOpportunities() {
        log.info("Returning {} opportunities", opportunities.size());
        return new ArrayList<>(opportunities);
    }

    public void clearOpportunities() {
        opportunities.clear();
    }
}