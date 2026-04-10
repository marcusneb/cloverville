package com.example.cloverville_project;

public class TradeOffer {

    private String owner;
    private String tradeOffer;
    private String priceOrService;
    private String status;
    private Integer pointCost;

    // Needed for JSON export
    public TradeOffer() {}

    public TradeOffer(String owner, String tradeOffer, String priceOrService,
                      String status, Integer pointCost) {
        this.owner = owner;
        this.tradeOffer = tradeOffer;
        this.priceOrService = priceOrService;
        this.status = status;
        this.pointCost = pointCost;
    }

    // ---------------------------
    // Getters
    // ---------------------------
    public String getOwner() {
        return owner;
    }

    public String getTradeOffer() {
        return tradeOffer;
    }

    public String getPriceOrService() {
        return priceOrService;
    }

    public String getStatus() {
        return status;
    }

    public Integer getPointCost() {
        return pointCost;
    }

    // ---------------------------
    // Setters
    // ---------------------------
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTradeOffer(String tradeOffer) {
        this.tradeOffer = tradeOffer;
    }

    public void setPriceOrService(String priceOrService) {
        this.priceOrService = priceOrService;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPointCost(Integer pointCost) {
        this.pointCost = pointCost;
    }


    @Override
    public String toString() {
        return tradeOffer + " (Owner: " + owner + ")";
    }
}
