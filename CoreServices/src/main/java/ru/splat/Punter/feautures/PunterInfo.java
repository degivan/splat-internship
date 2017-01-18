package ru.splat.Punter.feautures;

import ru.splat.facade.feautures.TransactionRequest;

public class PunterInfo implements TransactionRequest
{

    private String localTask;
    private int punterId;
    private long transactionId;

    public PunterInfo()
    {
    }

    public PunterInfo(int punterId, long transactionId)
    {
        this.punterId = punterId;
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PunterInfo punterInfo = (PunterInfo) o;

        return transactionId == punterInfo.transactionId;

    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    public PunterInfo(int punterId, long transactionId, String localTask)
    {
        this.punterId = punterId;
        this.transactionId = transactionId;
        this.localTask = localTask;
    }

    public int getPunterId() {
        return punterId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setPunterId(int punterId) {
        this.punterId = punterId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getLocalTask() {
        return localTask;
    }

    public void setLocalTask(String localTask) {
        this.localTask = localTask;
    }


}