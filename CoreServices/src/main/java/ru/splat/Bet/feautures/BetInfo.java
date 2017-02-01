package ru.splat.Bet.feautures;


import ru.splat.facade.feautures.TransactionRequest;
import ru.splat.messages.BetRequest;

import java.util.List;


public class BetInfo implements TransactionRequest
{

    private long transactionId;
    private int localTask;
    private BetRequest.Bet blob;
    private List<Integer> services;
    private long id;

    public BetInfo(long transactionId, int localTask, BetRequest.Bet blob, List<Integer> services)
    {
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.blob = blob;
        this.services = services;
    }

    public BetInfo(long transactionId, int localTask, BetRequest.Bet blob, List<Integer> services, long id)
    {
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.blob = blob;
        this.services = services;
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BetInfo betInfo = (BetInfo) o;

        return transactionId == betInfo.transactionId;

    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    @Override
    public String toString() {
        return "BetInfo{" +
                "transactionId=" + transactionId +
                ", localTask=" + localTask +
                ", blob=" + blob +
                ", services=" + services +
                ", id=" + id +
                '}';
    }

    public BetRequest.Bet getBlob() {
        return blob;
    }

    @Override
    public long getTransactionId() {
        return transactionId;
    }

    @Override
    public int getLocalTask() {
        return localTask;
    }

    @Override
    public List<Integer> getServices() {
        return services;
    }

    public long getId() {
        return id;
    }
}
