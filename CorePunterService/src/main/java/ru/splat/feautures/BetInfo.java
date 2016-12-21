package ru.splat.feautures;

public class BetInfo {

    private int punterId;
    private long transactionId;

    public BetInfo() {
    }

    public BetInfo(int punterId, long transactionId) {
        this.punterId = punterId;
        this.transactionId = transactionId;
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

    @Override
    public int hashCode() {
        int result = punterId;
        result = 31 * result + (int) (transactionId ^ (transactionId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BetInfo betInfo = (BetInfo) o;

        if (punterId != betInfo.punterId) return false;
        return transactionId == betInfo.transactionId;

    }
}