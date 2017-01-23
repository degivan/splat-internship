package ru.splat.facade.business;


import java.util.List;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.facade.feautures.TransactionRequest;



public interface BusinessService<T extends TransactionRequest>
{

    //TODO Вынести из репозиториев бизнес-логику.
    List<TransactionResult> processTransactions(List<T> transactionRequests);
}
