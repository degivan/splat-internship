package ru.splat.Punter.feautures;

import ru.splat.facade.feautures.TransactionRequest;

public class PunterInfo implements TransactionRequest
{

    private int localTask;
    private int punterId;
    private long transactionId;
    private String services;

    public PunterInfo()
    {
    }

    public PunterInfo(int punterId, long transactionId)
    {
        this.punterId = punterId;
        this.transactionId = transactionId;
    }

    public PunterInfo(int punterId, long transactionId, int localTask,String services)
    {
        this.punterId = punterId;
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.services = services;
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


    @Override
    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
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

    public int getLocalTask() {
        return localTask;
    }

    public void setLocalTask(int localTask) {
        this.localTask = localTask;
    }


}