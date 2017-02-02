package ru.splat.facade.service;


import org.slf4j.Logger;
import ru.splat.Punter.business.PunterBusinessService;
import ru.splat.facade.feautures.Limit;
import ru.splat.facade.feautures.Proxy;
import ru.splat.facade.feautures.TransactionRequest;
import ru.splat.facade.repository.LimitRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public interface LimitService<Request extends  TransactionRequest> {

    long DEQUE_SCAN_PERIOD_TIME = 20*60*1000;

    Logger LOGGER = getLogger(PunterBusinessService.class);

    ConcurrentMap<Integer,Proxy> dequeMap = new ConcurrentHashMap<>();

    Set<Integer> convertToSet(List<Request> requestList);

    default void addInDeque(List<Request> infoList, LimitRepository repository)
    {

        Set<Integer> integerSet = convertToSet(infoList);

        List<Integer> notContainsInDequeMap = integerSet.stream().filter(p -> !dequeMap.containsKey(p)).
                map(p -> p).collect(Collectors.toList());

        List<Limit> punterLimitList = repository.getLimits(notContainsInDequeMap);

        if (punterLimitList!=null && !punterLimitList.isEmpty())
            for (Limit punterLimit:punterLimitList)
            {
                Proxy proxy = dequeMap.putIfAbsent(
                        punterLimit.getId(),
                        new Proxy(punterLimit.getLimit(),punterLimit.getLimitTime(),new ArrayDeque<Long>()
                        ));

                if (proxy == null)
                {
                    LOGGER.info("In map have added id = " + punterLimit.getId());
                }
            }
    }

    default long scanDeque(long lastDeleteTime)
    {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastDeleteTime > DEQUE_SCAN_PERIOD_TIME)
        {
            LOGGER.info("Scan and delete unused limit");
            Iterator<Map.Entry<Integer, Proxy>> entries = dequeMap.entrySet().iterator();
            while (entries.hasNext())
            {
                Map.Entry<Integer, Proxy> entry = entries.next();
                Proxy proxy = entry.getValue();
                Deque<Long> deque = proxy.getDeque();
                LOGGER.info(" Actions for id = " + entry.getKey());
                while (!deque.isEmpty() && currentTime - deque.getFirst() > proxy.getLimitTime())
                {
                    LOGGER.info("Delete timestamp = " + deque.pollFirst());
                }
                LOGGER.info("Punter Deque size = " + deque.size());
                if (deque.isEmpty())
                {
                    LOGGER.info("Remove from Map");
                    entries.remove();
                }
            }

            lastDeleteTime = currentTime;
            LOGGER.info("Stop scan limit");
        }
        return lastDeleteTime;
    }
}
