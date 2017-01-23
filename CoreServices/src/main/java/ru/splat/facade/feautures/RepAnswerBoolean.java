package ru.splat.facade.feautures;


import ru.splat.facade.feautures.TransactionResponse;

public class RepAnswerBoolean implements TransactionResponse
{
    private long transactionId;
    private boolean result;
    private String services;

    public RepAnswerBoolean(long transactionId, boolean result, String services)
    {
        this.transactionId = transactionId;
        this.result = result;
        this.services = services;
    }

    @Override
    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public long getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }
}
