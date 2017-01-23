package ru.splat.Punter.business;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.facade.feautures.RepAnswerBoolean;
import ru.splat.facade.feautures.RepAnswerNothing;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.Punter.repository.PunterRepository;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.facade.business.BusinessService;
import ru.splat.messages.Response;
import ru.splat.messages.TaskTypesEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class PunterBusinessService implements BusinessService<PunterInfo>
{

    @Autowired
    private PunterRepository punterRepository;

    private Function<RepAnswerBoolean,TransactionResult>
            converterUpdateLimit = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).setBooleanResult(map.isResult()).build()
    );

    private Function<RepAnswerNothing,TransactionResult>
            converterCancel = (map) -> new TransactionResult(
            map.getTransactionId(),
            Response.ServiceResponse.newBuilder().setServices(map.getServices()).build()
    );

    @Override
    public List<TransactionResult> processTransactions(List<PunterInfo> transactionRequests)
    {
        Map<Integer, Set<PunterInfo>> localTaskComplex = new HashMap<>();

        for (PunterInfo punterInfo : transactionRequests)
        {
            if (!localTaskComplex.containsKey(punterInfo.getLocalTask()))
            {
                localTaskComplex.put(punterInfo.getLocalTask(), new HashSet<>());
            }
            localTaskComplex.get(punterInfo.getLocalTask()).add(punterInfo);
        }

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Set<PunterInfo>> entry : localTaskComplex.entrySet())
        {
           if (entry.getKey() == TaskTypesEnum.ADD_PUNTER_LIMITS.ordinal())
                    results.addAll(punterRepository.updateLimit(entry.getValue()).stream().
                            map((map) -> converterUpdateLimit.apply(map)).collect(Collectors.toList()));
            else if (entry.getKey() == TaskTypesEnum.CANCEL_PUNTER_LIMITS.ordinal())
                    results.addAll(punterRepository.cancelLimit(new ArrayList<PunterInfo>(entry.getValue())).stream().
                            map((map) -> converterCancel.apply(map)).collect(Collectors.toList()));
        }
        return results;
    }
}
