package ru.splat.Billing.feautures;


import ru.splat.facade.feautures.TransactionRequest;

import java.util.List;

public class BillingInfo implements TransactionRequest
{

    private int punterId;
    private int sum;
    private long transactionId;
    private int localTask;
    private List<Integer> services;

    public BillingInfo(int punterID, int sum, long transactionId, int localTask, List<Integer> services)
    {
        this.punterId = punterID;
        this.sum = sum;
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.services = services;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BillingInfo that = (BillingInfo) o;

        return transactionId == that.transactionId;

    }

    @Override
    public String toString() {
        return "BillingInfo{" +
                "punterId=" + punterId +
                ", sum=" + sum +
                ", transactionId=" + transactionId +
                ", localTask=" + localTask +
                ", services=" + services +
                '}';
    }

    @Override
    public List<Integer> getServices() {
        return services;
    }

    public void setServices(List<Integer> services) {
        this.services = services;
    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    public void setTransactionIc(long transactionID)
    {

        this.transactionId = transactionID;
    }

    public long getTransactionId()
    {

        return transactionId;
    }

    @Override
    public int getLocalTask()
    {
        return localTask;
    }

    public void setPunterID(int punterID)
    {

        this.punterId = punterID;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getPunterID()
    {
        return punterId;
    }

    public int getSum() {
        return sum;
    }
}
