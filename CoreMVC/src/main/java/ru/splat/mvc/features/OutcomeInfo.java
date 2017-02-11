package ru.splat.mvc.features;


public class OutcomeInfo
{

    private int outcomeId;
    private String name;
    private double coefficient;
    private int marketId;

    public int getId() {
        return outcomeId;
    }

    public void setId(int id) {
        this.outcomeId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {this.coefficient = coefficient;}

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }
}
