package ru.splat.messages.bet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class BetStateResponse { //пока что содержит только состояние ставки
    private BetState betState;

    public BetStateResponse(BetState betState) {

        this.betState = betState;
    }
    @Override
    public String toString() {
        return "BetStateRequest{state=" +
                 betState.toString() +
                '}';
    }
}
