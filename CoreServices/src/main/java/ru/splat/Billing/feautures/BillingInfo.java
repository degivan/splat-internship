package ru.splat.Billing.feautures;


import ru.splat.facade.feautures.TransactionRequest;

public class BillingInfo implements TransactionRequest{

    private int punterId;
    private int sum;
    private long transactionId;
    private String localTask;

    public BillingInfo(int punterID, int sum, long transactionId, String localTask)
    {
        this.punterId = punterID;
        this.sum = sum;
        this.transactionId = transactionId;
        this.localTask = localTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BillingInfo that = (BillingInfo) o;

        return transactionId == that.transactionId;

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
    public String getLocalTask()
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
