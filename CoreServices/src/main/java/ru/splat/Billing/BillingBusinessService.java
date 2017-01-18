package ru.splat.Billing;


import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.Billing.repository.BillingRepository;
import ru.splat.facade.business.BusinessService;
import ru.splat.facade.feautures.TransactionResult;
import java.util.*;
import java.util.stream.Collectors;

public class BillingBusinessService implements BusinessService<BillingInfo>
{

    @Autowired
    private BillingRepository billingRepository;


    @Override
    public List<TransactionResult> processTransactions(List<BillingInfo> transactionRequests) {
        Map<String, List<BillingInfo>> localTaskComplex = new HashMap<>();

        for (BillingInfo billingInfo: transactionRequests)
        {
            if (!localTaskComplex.containsKey(billingInfo.getLocalTask()))
            {
                localTaskComplex.put(billingInfo.getLocalTask(), new ArrayList<>());
            }
            localTaskComplex.get(billingInfo.getLocalTask()).add(billingInfo);
        }

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<String, List<BillingInfo>> entry : localTaskComplex.entrySet()) {
            switch (entry.getKey()) {
                case FIRST_PHASE:
                    results.addAll(billingRepository.phase1(entry.getValue()).stream().
                            map((map) -> converter.apply(map)).collect(Collectors.toList()));
                    break;
                case CANCEL_PHASE:
                    results.addAll(billingRepository.cancel(entry.getValue()).stream().
                            map((map) -> converter.apply(map)).collect(Collectors.toList()));
                    break;
            }
        }
        return results;
    }
}
