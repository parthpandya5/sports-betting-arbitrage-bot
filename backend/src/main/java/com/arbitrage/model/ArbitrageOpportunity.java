package com.arbitrage.model;

public class ArbitrageOpportunity {
    private String sport;
    private String event;
    private String market;
    private String sportsbook1;
    private double odds1;
    private String sportsbook2;
    private double odds2;
    private double roi;
    private double stake1Percentage;
    private double stake2Percentage;
    private double estimatedProfit;
    private long timestamp;

    public ArbitrageOpportunity() {}

    public ArbitrageOpportunity(String sport, String event, String market, 
                                String sportsbook1, double odds1, String sportsbook2,
                                double odds2, double roi, double stake1Percentage,
                                double stake2Percentage, double estimatedProfit, long timestamp) {
        this.sport = sport;
        this.event = event;
        this.market = market;
        this.sportsbook1 = sportsbook1;
        this.odds1 = odds1;
        this.sportsbook2 = sportsbook2;
        this.odds2 = odds2;
        this.roi = roi;
        this.stake1Percentage = stake1Percentage;
        this.stake2Percentage = stake2Percentage;
        this.estimatedProfit = estimatedProfit;
        this.timestamp = timestamp;
    }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
    
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    
    public String getSportsbook1() { return sportsbook1; }
    public void setSportsbook1(String sportsbook1) { this.sportsbook1 = sportsbook1; }
    
    public double getOdds1() { return odds1; }
    public void setOdds1(double odds1) { this.odds1 = odds1; }
    
    public String getSportsbook2() { return sportsbook2; }
    public void setSportsbook2(String sportsbook2) { this.sportsbook2 = sportsbook2; }
    
    public double getOdds2() { return odds2; }
    public void setOdds2(double odds2) { this.odds2 = odds2; }
    
    public double getRoi() { return roi; }
    public void setRoi(double roi) { this.roi = roi; }
    
    public double getStake1Percentage() { return stake1Percentage; }
    public void setStake1Percentage(double stake1Percentage) { this.stake1Percentage = stake1Percentage; }
    
    public double getStake2Percentage() { return stake2Percentage; }
    public void setStake2Percentage(double stake2Percentage) { this.stake2Percentage = stake2Percentage; }
    
    public double getEstimatedProfit() { return estimatedProfit; }
    public void setEstimatedProfit(double estimatedProfit) { this.estimatedProfit = estimatedProfit; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}