package ru.splat.facade.util;


import ru.splat.facade.feautures.Limit;
import ru.splat.facade.feautures.Proxy;
import ru.splat.facade.repository.LimitRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

//TODO описать ф-ии, зависимость лимитов

public class JmxUtil
{


    /**
     *@param limit - содержит в себе количественный (N) и временной (T) лимиты игрока/исхода
     *@param repository - репозиторий, из которого получают лимит
     *@param dequeMap - мапа, ключом которой является идентификатор игрока/исхода, а
     * значением количественный и временной лимиты, и также дек, хранящий в себе
     * список моментов времени, в которые были сделаны ставки по игроку/исходу
     *
     * В случае отказа работы Punter и Event сервисов и при их последующем возобновлении работы,
     * сервисы на вход могут получить не актуальные(устаревшие) по времени запросы. В данном случае,
     * если мы будем рассматривать все запросы, включая устаревшие, то сможем дать возможность
     * поставить игрокам больше ставок, тем самым увеличить прибыль заказчика,
     * но с другой стороны мы можем получить превышение лимитов, которые все-таки не зря используются.
     * После разговоров с заказчиком принято решение, что после восстановления сервиса
     * не актуальные по времени запросы игнорировать. И для рассмотрения отбирать ставки, имеющие минимальную
     * разницу с текущим временем.
     *
     *
     * В соответствии с принятым решением у нас возникает двусмысленная ситуация:
     * 1. При восстановлении работы сервиса. При выборе в качестве лимитов 2*T и 2*N мы получаем возможность
     * принять больше ставок, нежели при выборе T и N.
     * 2. В случае отмены резервирования лимитов. На данный момент, для упрощения работы сервисов,
     * отменяется самый последний зарезервированный лимит, у которого минимальная разница с текущим временем.
     * При таком раскладе, при выборе в качестве лимитов 2*N и 2*T, мы получаем временное окно, дающее
     * возможность обойти лимиты и зарезервировать >2*N лимитов. Если же в качестве  лимитов взять N и T,
     * то возможность обойти лимиты и зарезервировать >N лимитов.
     */
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
