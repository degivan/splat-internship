package ru.splat.facade.feautures;


public class RepAnswer
{
    private long transactionId;
    private boolean result;
    private String resultReason;

    public RepAnswer()
    {
    }

    public RepAnswer(long transactionId, boolean result, String resultReason)
    {
        this.transactionId = transactionId;
        this.result = result;
        this.resultReason = resultReason;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public boolean isResult() {
        return result;
    }

    public String getResultReason() {
        return resultReason;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setResultReason(String resultReason) {
        this.resultReason = resultReason;
    }
}
