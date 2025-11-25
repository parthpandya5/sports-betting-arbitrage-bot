package com.arbitrage.model;

public class BettingOdds {
    private String sport;
    private String event;
    private String market;
    private String sportsbook;
    private double odds;
    private String outcome;
    private long timestamp;

    public BettingOdds() {}

    public BettingOdds(String sport, String event, String market, String sportsbook, 
                       double odds, String outcome, long timestamp) {
        this.sport = sport;
        this.event = event;
        this.market = market;
        this.sportsbook = sportsbook;
        this.odds = odds;
        this.outcome = outcome;
        this.timestamp = timestamp;
    }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
    
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    
    public String getSportsbook() { return sportsbook; }
    public void setSportsbook(String sportsbook) { this.sportsbook = sportsbook; }
    
    public double getOdds() { return odds; }
    public void setOdds(double odds) { this.odds = odds; }
    
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}