package ru.splat.Billing.feautures;


public class BillingInfo {
    private int punterID;
    private int sum;
    private long transactionId;

    public BillingInfo(int punterID, int sum, long transactionId) {
        this.punterID = punterID;
        this.sum = sum;
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BillingInfo that = (BillingInfo) o;

        if (punterID != that.punterID) return false;
        return transactionId == that.transactionId;

    }

    @Override
    public int hashCode() {
        int result = punterID;
        result = 31 * result + (int) (transactionId ^ (transactionId >>> 32));
        return result;
    }

    public void setTransactionIc(long transactionID) {

        this.transactionId = transactionID;
    }

    public long getTransactionId() {

        return transactionId;
    }

    public void setPunterID(int punterID) {

        this.punterID = punterID;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getPunterID() {

        return punterID;
    }

    public int getSum() {
        return sum;
    }
}
