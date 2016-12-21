package ru.splat.feautures;

/**
 * Created by Rustam on 12/14/2016.
 */
public class TransactionResult {

    private long transactionId;
    private boolean result;

    public TransactionResult() {
    }

    public TransactionResult(long transactionId, boolean result) {
        this.transactionId = transactionId;
        this.result = result;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public boolean getResult() {
        return result;
    }

}
