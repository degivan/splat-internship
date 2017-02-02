package ru.splat.facade.util;


import ru.splat.facade.feautures.Limit;
import ru.splat.facade.feautures.Proxy;
import ru.splat.facade.repository.LimitRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class JmxUtil
{

    public static void set(Limit limit, LimitRepository repository, ConcurrentMap<Integer,Proxy> dequeMap)
    {

        int id = limit.getId();
        int lim = limit.getLimit();
        int limitTime = limit.getLimitTime();

        Limit outcomeLimit = new Limit(id, lim, limitTime);
        List<Integer> element = new ArrayList<>(1);
        element.add(outcomeLimit.getId());
        List<Limit> existElem = repository.getLimits(element);


        if (existElem != null && !existElem.isEmpty())
        {
            repository.updateLimit(limit);

            if (dequeMap.putIfAbsent(id, new Proxy(lim, limitTime, new ArrayDeque<Long>())) != null)
            {
                Proxy proxy = dequeMap.get(id);
                if (proxy != null)
                {
                    proxy.setLimit(lim);
                    proxy.setLimitTime(limitTime);
                }
            }
        }
    }


    public static String getLimit(int id, LimitRepository repository, ConcurrentMap<Integer,Proxy> dequeMap)
    {
        int result = 0;
        int limit = 0;
        String answer;
        Proxy proxy = dequeMap.get(id);
        long currentTime = System.currentTimeMillis();
        if (proxy != null)
        {
            Deque<Long> deque = proxy.getDeque();
            if (deque != null)
            {
                for (Long p : deque) {
                    if (currentTime - p < proxy.getLimitTime()) {
                        result++;
                    }
                }
                result = proxy.getLimit() - result;
                limit = proxy.getLimit();
            }
        }
        else
        {
            List<Integer> element = new ArrayList<>(1);
            element.add(id);
            List<Limit> existElem = repository.getLimits(element);

            if (existElem == null || existElem.isEmpty())
            {
                answer = "Id = " + id + " doesnt exists.";
                return answer;
            }
            result = existElem.get(0).getLimit();
            limit = result;
        }

        if (result < 0)
            result = 0;

        answer = "Id = " + id + " has " + result + " free limits from " + limit;
        return answer;
    }


}
