package ru.splat.messages.uptm.trmetadata;

/**
 * Created by Дмитрий on 01.02.2017.
 */
public class BetOutcome {
    private final Long outcomeId;
    private final Long eventId;
    private final Double coefficient;

    public BetOutcome(Long outcomeId, Long eventId, Double coef) {
        this.outcomeId = outcomeId;
        this.eventId = eventId;
        this.coefficient = coef;
    }
    public Long getOutcomeId() {
        return outcomeId;
    }
    public Long getEventId() {
        return eventId;
    }
    public Double getCoefficient() {
        return coefficient;
    }
}