package ru.splat.facade.feautures;



public class RepAnswerNothing implements TransactionResponse
{

    private long transactionId;
    private String services;

    public RepAnswerNothing(long transactionId,String services) {
        this.transactionId = transactionId;
        this.services = services;
    }

    @Override
    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
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
