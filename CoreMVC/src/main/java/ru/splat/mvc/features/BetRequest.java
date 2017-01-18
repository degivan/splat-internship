package ru.splat.mvc.features;


public class BetRequest
{

    private double coefficient;
    private int id;
    private int marketId;

    public BetRequest(){}

    public BetRequest(double coefficient, int id, int marketId)
    {
        this.coefficient = coefficient;
        this.id = id;
        this.marketId = marketId;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }
}
