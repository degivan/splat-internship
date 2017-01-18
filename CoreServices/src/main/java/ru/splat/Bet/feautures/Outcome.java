package ru.splat.Bet.feautures;

public class Outcome
{

    private int id;
    private int eventId;
    private double koef;

    public Outcome(int id, int eventId, double koef) {
        this.id = id;
        this.eventId = eventId;
        this.koef = koef;
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

    public double getKoef() {
        return koef;
    }

    public void setKoef(double koef) {
        this.koef = koef;
    }
}
