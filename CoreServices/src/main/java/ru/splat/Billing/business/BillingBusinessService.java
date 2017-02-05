package ru.splat.Billing.business;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.Billing.feautures.PunterBallance;
import ru.splat.Billing.repository.BillingRepository;
import ru.splat.facade.business.BusinessService;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.conventions.TaskTypesEnum;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class BillingBusinessService implements BusinessService<BillingInfo>
{

    @Autowired
    private BillingRepository billingRepository;

    private Logger LOGGER = getLogger(BillingBusinessService.class);

    @Override
    public List<TransactionResult> processTransactions(List<BillingInfo> transactionRequests)
    {
        LOGGER.info("processTransaction start");
        Map<Integer, List<BillingInfo>> localTaskComplex = new TreeMap<>(Collections.reverseOrder());

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
                    results.addAll(withdrow(entry.getValue()));
            else
            if (entry.getKey() == TaskTypesEnum.CANCEL_RESERVE.ordinal())
                    results.addAll(cancelReserve(entry.getValue()));
        }
        LOGGER.info("processTransaction stop");
        return results;
    }

    private Set<TransactionResult> withdrow(List<BillingInfo> billingInfoList)
    {
        LOGGER.info("Witdrow start");
        LOGGER.info("Withdrow array: ");
        LOGGER.info(Arrays.toString(billingInfoList.toArray()));
        List<Integer> punterIdList = billingInfoList.stream().map(billingInfo -> billingInfo.getPunterID()).collect(Collectors.toList());
        LOGGER.info("Insert ballance if not exist");
        billingRepository.insertPunterBallance(punterIdList);

        LOGGER.info("Punter sum from DB array: ");
        List<PunterBallance> ballance = billingRepository.getPunterBallance(punterIdList);
        LOGGER.info(Arrays.toString(ballance.toArray()));
        Map<Integer,Integer> map = new HashMap<>();
        ballance.stream().forEach(punterBallance -> map.put(punterBallance.getPunterId(),punterBallance.getSum()));

        Set<TransactionResult> transactionResults = new HashSet<>();

        boolean check;
        List<BillingInfo> forPay = new ArrayList<>(billingInfoList.size());
        for (BillingInfo billingInfo: billingInfoList)
        {
            check = map.get(billingInfo.getPunterID()) >= billingInfo.getSum();

            if (check) forPay.add(billingInfo);

            transactionResults.add(new TransactionResult(
                    billingInfo.getTransactionId(),
                    Response.ServiceResponse.newBuilder()
                            .setResult(check?ServiceResult.CONFIRMED.ordinal():ServiceResult.DENIED.ordinal())
                            .addAllServices(billingInfo.getServices()).build()
            ));
        }

        billingRepository.pay(forPay,true);

        LOGGER.info("Withdrow stop");
        return transactionResults;
    }

    private Set<TransactionResult> cancelReserve(List<BillingInfo> billingInfoList)
    {
        boolean check = false;
        LOGGER.info("Cancel start");
        LOGGER.info("Cancel array: ");
        LOGGER.info(Arrays.toString(billingInfoList.toArray()));
        billingRepository.pay(billingInfoList,check);
        LOGGER.info("Cancel stop");
        return billingInfoList.stream().map(billingInfo -> new TransactionResult(
                billingInfo.getTransactionId(),
                Response.ServiceResponse.newBuilder().setResult(ServiceResult.CONFIRMED.ordinal())
                        .addAllServices(billingInfo.getServices()).build()
        )).collect(Collectors.toSet());

    }

    @Override
    public void commitBusinessService() {

    }

    @Override
    public void rollbackBusinessSerivce() {

    }
}
