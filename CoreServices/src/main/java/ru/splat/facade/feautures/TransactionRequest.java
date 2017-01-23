package ru.splat.facade.feautures;


public interface TransactionRequest
{
    long getTransactionId();
    int getLocalTask();
    String getServices();
}

