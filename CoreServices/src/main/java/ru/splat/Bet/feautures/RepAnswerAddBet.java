package ru.splat.Bet.feautures;

import com.google.protobuf.Message;
import ru.splat.facade.feautures.TransactionResponse;


public class RepAnswerAddBet implements TransactionResponse
{

    private long transactionId;
    private int id;
    private String services;

    public RepAnswerAddBet(long transactionId, int id, String services)
    {
        this.transactionId = transactionId;
        this.id = id;
        this.services = services;
    }

    @Override
    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
