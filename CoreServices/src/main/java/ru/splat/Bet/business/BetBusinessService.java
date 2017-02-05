package ru.splat.Bet.business;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Bet.feautures.BetInfo;
import ru.splat.Bet.repository.BetRepository;
import ru.splat.facade.business.BusinessService;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.conventions.TaskTypesEnum;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class BetBusinessService implements BusinessService<BetInfo>
{

    private Logger LOGGER = getLogger(BetBusinessService.class);

    @Autowired
    BetRepository betRepository;

    @Override
    public List<TransactionResult> processTransactions(List<BetInfo> transactionRequests)
    {

        LOGGER.info("Start processTransaction");

        Map<Integer, List<BetInfo>> localTaskComplex = new TreeMap<>(Collections.reverseOrder());

        for (BetInfo betInfo: transactionRequests)
        {
            if (!localTaskComplex.containsKey(betInfo.getLocalTask()))
            {
                localTaskComplex.put(betInfo.getLocalTask(), new ArrayList<>());
            }
            localTaskComplex.get(betInfo.getLocalTask()).add(betInfo);
        }

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<Integer, List<BetInfo>> entry : localTaskComplex.entrySet())
        {
            if (entry.getKey() == TaskTypesEnum.ADD_BET.ordinal())
                    results.addAll(addBet(entry.getValue()));
            else
            if (entry.getKey() == TaskTypesEnum.CANCEL_BET.ordinal())
                    results.addAll(fixBet(entry.getValue(),"FAILED"));
            else
            if (entry.getKey() == TaskTypesEnum.FIX_BET.ordinal())
                results.addAll(fixBet(entry.getValue(),"SUCESSEFULL"));

        }
        LOGGER.info("Stop processTransaction");
        return results;
    }

    private Set<TransactionResult> addBet(List<BetInfo> betInfoList)
    {
        LOGGER.info("Start Add Bet");
        int sequence = betRepository.getCurrentSequenceVal();
        betRepository.addBet(betInfoList);
        LOGGER.info("Add bet array: ");
        LOGGER.info(Arrays.toString(betInfoList.toArray()));
        Set<TransactionResult> transactionalResult = new HashSet<>(betInfoList.size());
        for (BetInfo betInfo: betInfoList)
        {
            sequence++;

            transactionalResult.add(new TransactionResult(
                    betInfo.getTransactionId(),
                    Response.ServiceResponse.newBuilder().addAllServices(betInfo.getServices())
                            .setResult(ServiceResult.CONFIRMED.ordinal()).setLongAttachment(sequence).build()

            ));
        }
        LOGGER.info("Stop Add Bet");
        return transactionalResult;
    }

    private Set<TransactionResult> fixBet(List<BetInfo> betInfoList, String state)
    {
        LOGGER.info("Start fix state = " + state);
        LOGGER.info("Array for fix state = " + state + " : ");
        LOGGER.info(Arrays.toString(betInfoList.toArray()));
        betRepository.fixBetState(betInfoList,state);
        LOGGER.info("Stop fix state = " + state);
        return betInfoList.stream().map(map -> new TransactionResult(
                map.getTransactionId(),
                Response.ServiceResponse.newBuilder()
                        .setResult(ServiceResult.CONFIRMED.ordinal()).addAllServices(map.getServices()).build()
        )).collect(Collectors.toSet());
    }

    @Override
    public void commitBusinessService() {

    }

    @Override
    public void rollbackBusinessSerivce() {

    }
}
