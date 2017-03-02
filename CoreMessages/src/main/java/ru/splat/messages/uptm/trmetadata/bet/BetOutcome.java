package ru.splat.messages.uptm.trmetadata.bet;

/**
 * Created by Дмитрий on 01.02.2017.
 */
public class BetOutcome {
    private Integer outcomeId;
    private Integer eventId;
    private Double coefficient;
    private Integer marketId;

    public BetOutcome() {
    }

    public BetOutcome(Integer marketId, Integer eventId, Integer outcomeId, Double coefficient) {
        this.outcomeId = outcomeId;
        this.eventId = eventId;
        this.coefficient = coefficient;
        this.marketId = marketId;
    }

    @Override
    public String toString() {
        return "BetOutcome{" +
                "outcomeId=" + outcomeId +
                ", eventId=" + eventId +
                ", coefficient=" + coefficient +
                ", marketId=" + marketId +
                '}';
    }

    public Integer getOutcomeId() {
        return outcomeId;
    }
    public Integer getEventId() {
        return eventId;
    }
    public Double getCoefficient() {
        return coefficient;
    }

    public Integer getMarketId() {
        return marketId;
    }

    public void setOutcomeId(Integer outcomeId) {
        this.outcomeId = outcomeId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public void setCoefficient(Double coefficient) {
        this.coefficient = coefficient;
    }

    public void setMarketId(Integer marketId) {
        this.marketId = marketId;
    }

}