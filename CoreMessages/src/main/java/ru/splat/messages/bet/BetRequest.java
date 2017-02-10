package ru.splat.messages.bet;

public class BetRequest
{

    private double coefficient;
    private int id;
    private int eventId;

    public BetRequest(){}

    public BetRequest(double coefficient, int id, int eventId)
    {
        this.coefficient = coefficient;
        this.id = id;
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "BetRequest{" +
                "coefficient=" + coefficient +
                ", id=" + id +
                ", eventId=" + eventId +
                '}';
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

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
