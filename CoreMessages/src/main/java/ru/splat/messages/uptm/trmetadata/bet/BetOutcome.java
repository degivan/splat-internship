package ru.splat.messages.uptm.trmetadata.bet;

/**
 * Created by Дмитрий on 01.02.2017.
 */
public class BetOutcome {
    private final Integer outcomeId;
    private final Integer eventId;
    private final Double coefficient;

    public BetOutcome(Integer outcomeId, Integer eventId, Double coef) {
        this.outcomeId = outcomeId;
        this.eventId = eventId;
        this.coefficient = coef;
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
}