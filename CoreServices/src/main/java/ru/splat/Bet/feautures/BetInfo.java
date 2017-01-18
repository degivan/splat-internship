package ru.splat.Bet.feautures;

import ru.splat.facade.feautures.TransactionRequest;

import java.util.List;


public class BetInfo implements TransactionRequest {

    private long transactionId;
    private String localTask;
    private int punterId;
    private int betSum;
    private List<Outcome> outcomeList;

    public BetInfo(long transactionId, String localTask, int punterId, int betSum, List<Outcome> outcomeList) {
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.punterId = punterId;
        this.betSum = betSum;
        this.outcomeList = outcomeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BetInfo betInfo = (BetInfo) o;

        return transactionId == betInfo.transactionId;

    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setLocalTask(String localTask) {
        this.localTask = localTask;
    }

    public int getPunterId() {
        return punterId;
    }

    public void setPunterId(int punterId) {
        this.punterId = punterId;
    }

    public int getBetSum() {
        return betSum;
    }

    public void setBetSum(int betSum) {
        this.betSum = betSum;
    }

    public List<Outcome> getOutcomeList() {
        return outcomeList;
    }

    public void setOutcomeList(List<Outcome> outcomeList) {
        this.outcomeList = outcomeList;
    }



    @Override
    public long getTransactionId() {
        return transactionId;
    }

    @Override
    public String getLocalTask() {
        return localTask;
    }
}
