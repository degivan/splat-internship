package ru.splat.facade.business;


import java.util.List;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.facade.feautures.TransactionRequest;



public interface BusinessService<T extends TransactionRequest>
{
    List<TransactionResult> processTransactions(List<T> transactionRequests);

    void commitBusinessService();

    void rollbackBusinessSerivce();
}
