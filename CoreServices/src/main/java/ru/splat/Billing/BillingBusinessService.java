package ru.splat.Billing;


import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.facade.feautures.RepAnswerBoolean;
import ru.splat.Billing.repository.BillingRepository;
import ru.splat.facade.business.BusinessService;
import ru.splat.facade.feautures.RepAnswerNothing;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.messages.Response;
import ru.splat.messages.TaskTypesEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BillingBusinessService implements BusinessService<BillingInfo>
{

    @Autowired
    private BillingRepository billingRepository;

    private Function<RepAnswerBoolean,TransactionResult>
            converterWithdraw = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).setBooleanResult(map.isResult()).build()
    );

    private Function<RepAnswerNothing,TransactionResult>
            converterCancel = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).build()
    );

    @Override
    public List<TransactionResult> processTransactions(List<BillingInfo> transactionRequests)
    {
        Map<Integer, List<BillingInfo>> localTaskComplex = new HashMap<>();

        for (BillingInfo billingInfo: transactionRequests)
        {
            if (!localTaskComplex.containsKey(billingInfo.getLocalTask()))
            {
                localTaskComplex.put(billingInfo.getLocalTask(), new ArrayList<>());
            }
            localTaskComplex.get(billingInfo.getLocalTask()).add(billingInfo);
        }

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<Integer, List<BillingInfo>> entry : localTaskComplex.entrySet())
        {

            if (entry.getKey() == TaskTypesEnum.WITHDRAW.ordinal())
                    results.addAll(billingRepository.withdrow(entry.getValue()).stream().
                            map((map) -> converterWithdraw.apply(map)).collect(Collectors.toList()));

            if (entry.getKey() == TaskTypesEnum.CANCEL_RESERVE.ordinal())
                    results.addAll(billingRepository.cancel(entry.getValue()).stream().
                            map((map) -> converterCancel.apply(map)).collect(Collectors.toList()));
        }
        return results;
    }
}
