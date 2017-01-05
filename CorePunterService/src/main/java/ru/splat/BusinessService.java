package ru.splat;


import java.util.List;

import ru.splat.Billing.feautures.TransactionResult;


/**
 * Created by nkalugin on 1/5/17.
 */
public interface BusinessService<T extends TransactionRequest>
{
    List<TransactionResult> processTransactions(List<T> transactionRequests);
}
