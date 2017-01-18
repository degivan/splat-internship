package ru.splat.facade.feautures;


import com.google.protobuf.Message;

public class TransactionResult
{

    public TransactionResult() {}

    public TransactionResult(long transactionId, Message result)
    {
        this.transactionId = transactionId;
        this.result = result;
    }

    private long transactionId;
    private Message result;

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() { return transactionId;}

    public void setResult(Message result) {this.result = result;}

    public Message getResult()
    {
        return result;
    }

    @Override
    public String toString()
    {
        return "Transaction ID: " + transactionId + " " + result.toString();
    }

}
