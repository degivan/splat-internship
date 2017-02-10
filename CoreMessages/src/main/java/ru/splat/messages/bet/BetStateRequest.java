package ru.splat.messages.bet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class BetStateRequest {
    private long id;

    public BetStateRequest(long id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "BetStateRequest{id="+id+'}';
    }
}
