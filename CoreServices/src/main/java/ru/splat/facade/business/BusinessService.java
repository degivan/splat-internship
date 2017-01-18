package ru.splat.facade.business;


import java.util.List;
import java.util.function.Function;

import ru.splat.Billing.protobuf.BallanceRes;
import ru.splat.facade.feautures.RepAnswer;
import ru.splat.facade.feautures.TransactionResult;
import ru.splat.facade.feautures.TransactionRequest;



public interface BusinessService<T extends TransactionRequest>
{
    String FIRST_PHASE = "phase1";
    String SECOND_PHASE = "phase2";
    String CANCEL_PHASE = "cancel";

    Function<RepAnswer,TransactionResult>
            converter = (map) -> new TransactionResult(map.getTransactionId(), BallanceRes.Billing.newBuilder().setResult(map.isResult()).setResultReason(map.getResultReason()).build());


    List<TransactionResult> processTransactions(List<T> transactionRequests);
}
