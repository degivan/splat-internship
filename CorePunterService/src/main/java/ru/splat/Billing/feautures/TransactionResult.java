package ru.splat.Billing.feautures;


public class TransactionResult {

    private long transactionId;
    private boolean result;
    private String resultReason;



    public TransactionResult() {
    }

    public TransactionResult(long transactionId, boolean result, String resultReason) {
        this.transactionId = transactionId;
        this.result = result;
        this.resultReason = resultReason;
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

    public boolean isResult() {
        return result;
    }

    public String getResultReason() {
        return resultReason;
    }

    public void setResultReason(String resultReason) {
        this.resultReason = resultReason;
    }



}
