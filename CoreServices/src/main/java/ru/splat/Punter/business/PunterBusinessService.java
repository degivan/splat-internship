package ru.splat.Punter.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import ru.splat.facade.feautures.Proxy;
import ru.splat.facade.feautures.Limit;
import ru.splat.facade.service.LimitService;
import ru.splat.facade.util.JmxUtil;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.Punter.repository.PunterRepository;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.facade.business.BusinessService;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.TaskTypesEnum;
import java.util.*;
import java.util.stream.Collectors;

@ManagedResource(objectName = "Punter Limit Winodw:name=Resource")
public class PunterBusinessService implements BusinessService<PunterInfo>, LimitService<PunterInfo>
{

    @Autowired
    private PunterRepository punterRepository;

    private long lastDeleteTime;

    private Map<Integer,Long> commitAddDequeMap;

    private List<Integer> commitCancelDequeList;

    public PunterBusinessService()
    {
        lastDeleteTime = System.currentTimeMillis();
    }

    @ManagedOperation
    public String getPunterFreeLimitFrom(int id)
    {
        return JmxUtil.getLimit(id, punterRepository, dequeMap);
    }

    @ManagedOperation
    public synchronized void setPunterLimit(int id, int lim, int limitTime)
    {
        JmxUtil.set(new Limit(id, lim,limitTime),punterRepository,dequeMap);
    }

    @Override
    public Set<Integer> convertToSet(List<PunterInfo> punterInfoList)
    {
        return new HashSet<Integer>(punterInfoList.stream().map(punterInfo -> punterInfo.getId()).collect(Collectors.toSet()));
    }

    private List<TransactionResult> addPunterLimits(List<PunterInfo> punterInfoList)
    {
        long currentTime = System.currentTimeMillis();

        List<TransactionResult> result = new ArrayList<>(punterInfoList.size());
        LOGGER.info("Start Add Punter limits: ");
        LOGGER.info(Arrays.toString(punterInfoList.toArray()));

        addInDeque(punterInfoList, punterRepository);

        for (PunterInfo punterInfo: punterInfoList) {

            LOGGER.info("Punter id = " + punterInfo.getId());

            Proxy proxy = dequeMap.get(punterInfo.getId());

            boolean answer = false;

            if (currentTime - punterInfo.getTime() <= proxy.getLimitTime())
            {
                Deque<Long> deque = proxy.getDeque();
                while (!deque.isEmpty() && currentTime - deque.getFirst() > proxy.getLimitTime())
                    deque.pollFirst();

                if (deque.isEmpty() || deque.size() < proxy.getLimit())
                {
                    answer = true;
                  //  deque.addLast(punterInfo.getTime());
                    commitAddDequeMap.put(punterInfo.getId(),punterInfo.getTime());
                }
            }
            if (answer)
            {
                LOGGER.info("Reserve limit");
            }
            else
            {
                LOGGER.info("Don't reserve limit");
            }
            result.add(new TransactionResult(punterInfo.getTransactionId(),
                    Response.ServiceResponse.newBuilder().addAllServices(punterInfo.getServices()).setBooleanResult(answer).build()));
        }
        LOGGER.info("Stop Add Punter limits: ");
        return result;
    }

    private List<TransactionResult> cancelPunterLimits(List<PunterInfo> punterInfoList)
    {

        LOGGER.info("Start cancel punter limits");

        List<TransactionResult> result = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        for (PunterInfo p : punterInfoList)
        {

            LOGGER.info("Punter id = " + p.getId());
            if (dequeMap.containsKey(p.getId()))
            {
                Proxy proxy = dequeMap.get(p.getId());
                Deque<Long> deque = proxy.getDeque();
                while (!deque.isEmpty() && currentTime - deque.getFirst() > proxy.getLimitTime())
                    deque.pollFirst();

                if (!deque.isEmpty())
                {
                   commitCancelDequeList.add(p.getId());
                    LOGGER.info("Cancel Reserve limit");
                    // deque.pollFirst();
                }
                else
                {
                    LOGGER.info("Don't Cancel Reserve limit");
                }
            }
            else
            {
                LOGGER.info("Map hasnt same id");
            }

            result.add(new TransactionResult(
                    p.getTransactionId(),
                    Response.ServiceResponse.newBuilder().addAllServices(p.getServices()).build()
            ));
        }

        LOGGER.info("Stop cancel punter limits");
        return result;
    }

    @Override
    public List<TransactionResult> processTransactions(List<PunterInfo> transactionRequests)
    {
        LOGGER.info("Start processTransaction");


       lastDeleteTime = scanDeque(lastDeleteTime);

        Map<Integer, Set<PunterInfo>> localTaskComplex = new TreeMap<>(Collections.reverseOrder());

        for (PunterInfo punterInfo : transactionRequests)
        {
            if (!localTaskComplex.containsKey(punterInfo.getLocalTask()))
            {
                localTaskComplex.put(punterInfo.getLocalTask(), new HashSet<>());
            }
            localTaskComplex.get(punterInfo.getLocalTask()).add(punterInfo);
        }

        commitCancelDequeList = new ArrayList<>();
        commitAddDequeMap = new HashMap<>();

        List<TransactionResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Set<PunterInfo>> entry : localTaskComplex.entrySet())
        {
           if (entry.getKey() == TaskTypesEnum.ADD_PUNTER_LIMITS.ordinal())
           {
               LOGGER.info("Reserve limit array: ");
               LOGGER.info(Arrays.toString(entry.getValue().toArray()));
               results.addAll(addPunterLimits(entry.getValue().stream().
                       collect(Collectors.toList())));
           }
            else if (entry.getKey() == TaskTypesEnum.CANCEL_PUNTER_LIMITS.ordinal())
            {
                LOGGER.info("Cancel limit array: ");
               LOGGER.info(Arrays.toString(entry.getValue().toArray()));
               results.addAll(cancelPunterLimits(new ArrayList<PunterInfo>(entry.getValue())).stream().
                       collect(Collectors.toList()));
           }
        }
        LOGGER.info("Stop processTransaction");
        return results;
    }

    @Override
    public void commitBusinessService()
    {
        if (commitCancelDequeList != null)
        {
            commitCancelDequeList.forEach(p ->
                {
                    Proxy proxy = dequeMap.get(p);
                    if (proxy != null)
                    {
                        Deque<Long> deque = proxy.getDeque();
                        if (deque != null)
                        {
                            deque.pollFirst();
                            LOGGER.info("Delete from Deque for punter id = " + p);
                        }
                    }
                }
        );
            commitCancelDequeList = null;
        }

        if (commitAddDequeMap != null)
        {

            for (Map.Entry<Integer, Long> entry : commitAddDequeMap.entrySet())
            {
                Proxy proxy = dequeMap.get(entry.getKey());

                if (proxy != null)
                {
                    Deque<Long> deque = proxy.getDeque();
                    if (deque != null) {
                        deque.addLast(entry.getValue());
                        LOGGER.info("Add in Deque for punter id = " + entry.getKey());
                    }
                }
            }
            commitAddDequeMap = null;
        }
    }

    @Override
    public void rollbackBusinessSerivce()
    {
        LOGGER.info("Rollback");
        commitAddDequeMap = null;
        commitCancelDequeList = null;
    }
}
