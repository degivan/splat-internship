package ru.splat.facade.feautures;


import java.util.List;

public interface TransactionRequest
{
    long getTransactionId();
    int getLocalTask();
    List<Integer> getServices();
}

