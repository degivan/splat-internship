package ru.splat.messages.bet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class BetStateRequest {
    private long transactonId;
    private int userId;

    public BetStateRequest() {
    }

    public BetStateRequest(long transactonId, int userId) {
        this.transactonId = transactonId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "BetStateRequest{" +
                "transactionId=" + transactonId +
                ", userId=" + userId +
                '}';
    }

    public long getTransactonId() {
        return transactonId;
    }

    public void setTransactonId(long transactonId) {
        this.transactonId = transactonId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
