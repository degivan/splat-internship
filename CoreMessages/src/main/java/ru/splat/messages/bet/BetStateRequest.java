package ru.splat.messages.bet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class BetStateRequest {
    private long id;
    private int userId;

    public BetStateRequest(long id, int userId) {
        this.id = id;
        this.userId = userId;
    }
    @Override
    public String toString() {
        return "BetStateRequest{id="+id+'}';
    }
}
