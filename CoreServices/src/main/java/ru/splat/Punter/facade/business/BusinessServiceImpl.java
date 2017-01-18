package ru.splat.Punter.facade.business;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.facade.feautures.TransactionResult;
import ru.splat.Punter.facade.repository.PunterRepository;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.facade.business.BusinessService;

import java.util.*;
import java.util.stream.Collectors;


public class BusinessServiceImpl implements BusinessService<PunterInfo>
{

    @Autowired
    private PunterRepository punterRepository;

    @Override
    public List<TransactionResult> processTransactions(List<PunterInfo> transactionRequests)
    {
        Map<String, Set<PunterInfo>> localTaskComplex = new HashMap<>();

        for (PunterInfo punterInfo : transactionRequests)
        {
            if (!localTaskComplex.containsKey(punterInfo.getLocalTask()))
            {
                localTaskComplex.put(punterInfo.getLocalTask(), new HashSet<>());
            }
            localTaskComplex.get(punterInfo.getLocalTask()).add(punterInfo);
        }

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<String, Set<PunterInfo>> entry : localTaskComplex.entrySet())
        {
            switch (entry.getKey())
            {
                case FIRST_PHASE:
                    results.addAll(punterRepository.phase1(entry.getValue()).stream().
                            map((map) -> converter.apply(map)).collect(Collectors.toList()));
                    break;
                case CANCEL_PHASE:
                    results.addAll(punterRepository.cancel(new ArrayList<PunterInfo>(entry.getValue())).stream().
                            map((map) -> converter.apply(map)).collect(Collectors.toList()));
                    break;
            }
        }
        return results;
    }
}
