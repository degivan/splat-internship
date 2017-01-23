package ru.splat.Bet.business;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Bet.feautures.BetInfo;
import ru.splat.Bet.feautures.RepAnswerAddBet;
import ru.splat.Bet.repository.BetRepository;
import ru.splat.facade.business.BusinessService;
import ru.splat.facade.feautures.RepAnswerNothing;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.messages.Response;
import ru.splat.messages.TaskTypesEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BetBusinessService implements BusinessService<BetInfo>
{

    @Autowired
    BetRepository betRepository;

    private  Function<RepAnswerAddBet,TransactionResult>
            converterAddBet = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).setLongResult(map.getId()).build()
    );

    private  Function<RepAnswerNothing,TransactionResult>
            converterFixBet = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).build()
    );


    @Override
    public List<TransactionResult> processTransactions(List<BetInfo> transactionRequests)
    {
        Map<Integer, List<BetInfo>> localTaskComplex = new HashMap<>();

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
                    results.addAll(betRepository.addBet(entry.getValue()).stream().
                            map((map) -> converterAddBet.apply(map)).collect(Collectors.toList()));

            if (entry.getKey() == TaskTypesEnum.CACEL_BET.ordinal())
                    results.addAll(betRepository.fixBetState(entry.getValue(),"FAILED").stream().
                            map((map) -> converterFixBet.apply(map)).collect(Collectors.toList()));

            if (entry.getKey() == TaskTypesEnum.FIX_BET.ordinal())
                results.addAll(betRepository.fixBetState(entry.getValue(),"SUCESSEFULL").stream().
                        map((map) -> converterFixBet.apply(map)).collect(Collectors.toList()));;

        }
        return results;
    }
}
